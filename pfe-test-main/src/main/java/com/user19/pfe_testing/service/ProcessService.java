package com.user19.pfe_testing.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.user19.pfe_testing.dto.*;
import com.user19.pfe_testing.mapper.Mapper;
import com.user19.pfe_testing.model.*;
import com.user19.pfe_testing.model.enums.ProcessStatus;
import com.user19.pfe_testing.repository.*;
import com.user19.pfe_testing.util.ConditionEvaluator;
import com.user19.pfe_testing.util.KeycloakSecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProcessService {

    private final ProcessDefinitionRepository processDefinitionRepository;
    private final ProcessStepRepository processStepRepository;
    private final Mapper mapper;
    private final KeycloakSecurityUtil keycloakSecurityUtil;
    private final ProcessInstanceRepository processInstanceRepository;
    private final ProcessHistoryRepository processHistoryRepository;
    private final EmailService emailService;
    private final ApprovalStepRepository approvalStepRepository;
    private final NotificationStepRepository notificationStepRepository;
    private final ConditionStepRepository conditionStepRepository;
    private final ConditionEvaluator conditionEvaluator;

    @Transactional
    public void startProcess(SubmissionDTO submissionDTO) {
        ProcessDefinition processDefinition = processDefinitionRepository.findByFormSchemaId(submissionDTO.formSchemaId())
                .orElseThrow(() -> new EntityNotFoundException("Process definition not found"));

        String currentUserId = keycloakSecurityUtil.getCurrentUserId();
        String initialStepName = processDefinition.getSteps().getFirst().getName();

        ProcessInstance processInstance = ProcessInstance.builder()
                .processDefinition(processDefinition)
                .actorId(currentUserId)
                .status(ProcessStatus.PENDING)
                .currentStepName(initialStepName)
                .formData(submissionDTO.formData())
                .build();

        ProcessInstance savedProcessInstance = processInstanceRepository.save(processInstance);
        addProcessHistory(processInstance, ProcessStatus.STARTED.name(), currentUserId, null, ProcessStatus.PENDING);
        handleFirstStep(savedProcessInstance.getId());
    }

    private void handleFirstStep(Long processInstanceId) {
        ProcessInstance processInstance = getProcessInstanceById(processInstanceId);
        String currentStepName = processInstance.getCurrentStepName();

        if (isLastStep(currentStepName, processInstance)) {
            handleLastStep(processInstance);
            return;
        }

        ProcessStep currentProcessStep = processStepRepository.findByName(currentStepName);
        executeStepAction(currentProcessStep, processInstance);
    }

    public void moveToNextStep(Long processInstanceId) {
        ProcessInstance processInstance = getProcessInstanceById(processInstanceId);
        String currentStepName = processInstance.getCurrentStepName();

        if (isLastStep(currentStepName, processInstance)) {
            handleLastStep(processInstance);
            return;
        }

        ProcessStep currentProcessStep = processStepRepository.findByName(currentStepName);
        executeStepAction(currentProcessStep, processInstance);
        updateToNextStep(processInstance, currentProcessStep.getName());
    }

    private void executeStepAction(ProcessStep currentProcessStep, ProcessInstance processInstance) {
        if (currentProcessStep instanceof NotificationStep) {
            handleNotificationStep((NotificationStep) currentProcessStep);
        } else if (currentProcessStep instanceof ConditionStep) {
            handleConditionStep((ConditionStep) currentProcessStep, processInstance);
        } else if (currentProcessStep instanceof ApprovalStep) {
            handleApprovalStep((ApprovalStep) currentProcessStep);
        }
    }

    private void handleLastStep(ProcessInstance processInstance) {
        String clientName = keycloakSecurityUtil.getCurrentUserName(processInstance.getActorId());
        String clientEmail = keycloakSecurityUtil.getCurrentUserEmail(processInstance.getActorId());
        emailService.notifyClient(clientEmail, clientName, ProcessStatus.COMPLETED.name());
    }

    private void handleNotificationStep(NotificationStep notificationStep) {
        emailService.notifyReciptients(notificationStep);
    }

    private void handleConditionStep(ConditionStep conditionStep, ProcessInstance processInstance) {
        for (Condition condition : conditionStep.getConditions()) {
            if (conditionEvaluator.evaluateCondition(processInstance.getFormData(), condition.getCondition())) {
                updateCurrentStepFromCondition(processInstance.getId(), condition.getTargetStep());
                break;
            }
        }
    }

    private void handleApprovalStep(ApprovalStep approvalStep) {
        emailService.notifyValidators(approvalStep);
    }

    private void updateCurrentStepFromCondition(Long processInstanceId, String stepName) {
        ProcessInstance processInstance = getProcessInstanceById(processInstanceId);
        validateStepExists(processInstance, stepName);
        processInstance.setCurrentStepName(stepName);
        processInstanceRepository.save(processInstance);
    }

    private boolean isLastStep(String currentStepName, ProcessInstance processInstance) {
        return Objects.equals(processInstance.getProcessDefinition().getSteps().getLast().getName(), currentStepName);
    }

    private void updateToNextStep(ProcessInstance processInstance, String previousStepName) {
        List<ProcessStep> processSteps = processInstance.getProcessDefinition().getSteps();
        int nextStepIndex = processSteps.indexOf(processStepRepository.findByName(previousStepName)) + 1;
        processInstance.setCurrentStepName(processSteps.get(nextStepIndex).getName());
        processInstanceRepository.save(processInstance);
    }

    public void saveProcessDefinition(ProcessDefinitionDTO processDefinitionDTO) {
        if (processDefinitionRepository.count() > 0) {
            throw new IllegalStateException("A process definition already exists. Use update instead.");
        }

        ProcessDefinition processDefinition = ProcessDefinition.builder()
                .name(processDefinitionDTO.getName())
                .build();

        ProcessDefinition savedDefinition = processDefinitionRepository.save(processDefinition);
        saveProcessSteps(savedDefinition, processDefinitionDTO);
    }

    public void saveProcessSteps(ProcessDefinition definition, ProcessDefinitionDTO processDefinitionDTO) {
        for (ProcessStepDTO stepDTO : processDefinitionDTO.getSteps()) {
            switch (stepDTO.getStepType()) {
                case "APPROVAL" -> saveApprovalSteps(definition, stepDTO);
                case "CONDITION" -> saveConditionSteps(definition, stepDTO);
                case "NOTIFY" -> saveNotificationSteps(definition, stepDTO);
            }
        }
    }

    private void saveNotificationSteps(ProcessDefinition definition, ProcessStepDTO stepDTO) {
        notificationStepRepository.save((NotificationStep) mapper.convertStepDTOToEntity(stepDTO, definition));
    }

    private void saveApprovalSteps(ProcessDefinition definition, ProcessStepDTO stepDTO) {
        approvalStepRepository.save((ApprovalStep) mapper.convertStepDTOToEntity(stepDTO, definition));
    }

    private void saveConditionSteps(ProcessDefinition definition, ProcessStepDTO stepDTO) {
        ConditionStep conditionStep = (ConditionStep) mapper.convertStepDTOToEntity(stepDTO, definition);
        List<Condition> conditions = stepDTO.getCondition().stream()
                .map(mapper::conditionDTOToEntity)
                .toList();

        conditions.forEach(condition -> {
            condition.setConditionStep(conditionStep);
            conditionStep.getConditions().add(condition);
        });

        conditionStepRepository.save(conditionStep);
    }

    public void updateProcessDefinition(ProcessDefinitionDTO processDefinitionDTO) {
        ProcessDefinition processDefinition = processDefinitionRepository.findAll()
                .stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("No process definition found to update."));

        clearAllProcessSteps();
        processDefinition.setName(processDefinitionDTO.getName());
        ProcessDefinition savedProcessDefinition = processDefinitionRepository.save(processDefinition);

        saveProcessSteps(savedProcessDefinition, processDefinitionDTO);
        updateCurrentStepOfProcessInstance(savedProcessDefinition);
    }

    private void clearAllProcessSteps() {
        processStepRepository.deleteAll();
        approvalStepRepository.deleteAll();
        conditionStepRepository.deleteAll();
        notificationStepRepository.deleteAll();
    }

    private void updateCurrentStepOfProcessInstance(ProcessDefinition processDefinition) {
        List<ProcessInstance> filteredProcessInstances = filterProcessInstancesWithInvalidSteps(processDefinition);
        if (!filteredProcessInstances.isEmpty()) {
            for (ProcessInstance process : filteredProcessInstances) {
                List<ProcessHistory> processHistories = process.getHistory();
                String lastValidStep = getLastValidStepFromHistory(processHistories, processDefinition);
                updateToNextStep(process, lastValidStep);
            }
        }
    }

    private String getLastValidStepFromHistory(List<ProcessHistory> processHistories, ProcessDefinition processDefinition) {
        List<String> validStepNames = processDefinition.getSteps()
                .stream()
                .map(ProcessStep::getName)
                .toList();

        for (int i = processHistories.size() - 1; i >= 0; i--) {
            String action = processHistories.get(i).getAction();
            if (validStepNames.contains(action)) {
                return action;
            }
        }

        return validStepNames.isEmpty() ? null : validStepNames.get(0);
    }

    private List<ProcessInstance> filterProcessInstancesWithInvalidSteps(ProcessDefinition processDefinition) {
        List<String> validStepNames = processStepRepository.findAll().stream()
                .map(ProcessStep::getName)
                .toList();

        return processInstanceRepository.findAll().stream()
                .filter(instance -> !validStepNames.contains(instance.getCurrentStepName()))
                .toList();
    }

    public void addProcessHistory(ProcessInstance processInstance, String action, String actorId,
                                  String comment, ProcessStatus actionStatus) {
        ProcessHistory processHistory = ProcessHistory.builder()
                .processInstance(processInstance)
                .action(action)
                .actorId(actorId)
                .actionStatus(actionStatus)
                .timestamp(LocalDateTime.now())
                .comments(comment)
                .build();
        processHistoryRepository.save(processHistory);
    }

    public ProcessDefinitionDTO getProcessDefinition() {
        ProcessDefinition processDefinition = processDefinitionRepository.findAll().stream()
                .findFirst().orElse(null);

        return ProcessDefinitionDTO.builder()
                .id(processDefinition.getId())
                .name(processDefinition.getName())
                .steps(processDefinition.getSteps().stream()
                        .map(mapper::convertStepEntityToDTO)
                        .toList())
                .build();
    }

    public List<ProcessHistoryDTO> getProcessHistory() {
        String currentUserId = keycloakSecurityUtil.getCurrentUserId();
        ProcessInstance processInstance = processInstanceRepository.findByActorIdAndStatus(currentUserId, ProcessStatus.PENDING);

        return Optional.ofNullable(processInstance)
                .map(pi -> pi.getHistory().stream()
                        .map(mapper::processHistoryToDTO)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    public List<ReportDTO> getAllReports() {
        String processDefinitionName = processDefinitionRepository.findAll().stream()
                .findFirst()
                .map(ProcessDefinition::getName)
                .orElse("Unknown Process");

        String currentUserId = keycloakSecurityUtil.getCurrentUserId();
        return processInstanceRepository.findByActorId(currentUserId).stream()
                .map(processInstance -> buildReportDTO(processInstance, processDefinitionName))
                .collect(Collectors.toList());
    }

    private ReportDTO buildReportDTO(ProcessInstance processInstance, String processDefinitionName) {
        return new ReportDTO.ReportDTOBuilder()
                .processInstanceId(processInstance.getId())
                .processName(processDefinitionName)
                .processHistoryDTOList(processInstance.getHistory().stream()
                        .map(mapper::processHistoryToDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    public void cancelRequest(Long id) {
        ProcessInstance processInstance = getProcessInstanceById(id);
        processInstance.setStatus(ProcessStatus.CANCELLED);

        String currentUserId = keycloakSecurityUtil.getCurrentUserId();
        addProcessHistory(processInstance, ProcessStatus.CANCELLED.name(), currentUserId, null, ProcessStatus.CANCELLED);
    }

    private ProcessInstance getProcessInstanceById(Long id) {
        return processInstanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Process Instance not found: " + id));
    }

    private void validateStepExists(ProcessInstance processInstance, String stepName) {
        boolean stepExists = processInstance.getProcessDefinition().getSteps().stream()
                .anyMatch(step -> step.getName().equals(stepName));

        if (!stepExists) {
            throw new EntityNotFoundException("Process step not found: " + stepName);
        }
    }
}