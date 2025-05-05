package com.user19.pfe_testing.service;


import com.user19.pfe_testing.dto.*;
import com.user19.pfe_testing.mapper.Mapper;
import com.user19.pfe_testing.model.*;
import com.user19.pfe_testing.model.enums.ProcessStatus;
import com.user19.pfe_testing.repository.*;
import com.user19.pfe_testing.util.ConditionEvaluator;
import com.user19.pfe_testing.util.KeycloakSecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;


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
    private final FormSchemaRepository formSchemaRepository;


    @Transactional
    public void startProcess(SubmissionDTO submissionDTO) {
            ProcessDefinition processDefinition = processDefinitionRepository.findById(submissionDTO.processDefenitionId())
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
        addProcessHistory(processInstance, ProcessStatus.STARTED.name(), currentUserId, null, ProcessStatus.APPROVED);
        handleFirstStep(savedProcessInstance.getId());
    }

    private void handleFirstStep(Long processInstanceId) {
        ProcessInstance processInstance = getProcessInstanceById(processInstanceId);
        String currentStepName = processInstance.getCurrentStepName();

        if (isLastStep(currentStepName, processInstance)) {
            handleLastStep(processInstance);
            return;
        }

        ProcessStep currentProcessStep = processStepRepository.findByNameAndProcessDefinition(currentStepName, processInstance.getProcessDefinition());
        executeStepAction(currentProcessStep, processInstance);
    }

    public void moveToNextStep(Long processInstanceId) {
        ProcessInstance processInstance = getProcessInstanceById(processInstanceId);
        String currentStepName = processInstance.getCurrentStepName();

        if (isLastStep(currentStepName, processInstance)) {
            handleLastStep(processInstance);
            return;
        }

        ProcessStep currentProcessStep = processStepRepository.findByNameAndProcessDefinition(currentStepName, processInstance.getProcessDefinition());
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
        int nextStepIndex = processSteps.indexOf(processStepRepository.findByNameAndProcessDefinition(previousStepName, processInstance.getProcessDefinition())) + 1;
        processInstance.setCurrentStepName(processSteps.get(nextStepIndex).getName());
        processInstanceRepository.save(processInstance);
    }

    public void saveProcessDefinition(ProcessDefinitionDTO processDefinitionDTO) {
        ProcessDefinition processDefinition = ProcessDefinition.builder()
                .name(processDefinitionDTO.getName())
                .formSchemaId(processDefinitionDTO.getFormTemplate().getId())
                .build();

        ProcessDefinition savedDefinition = processDefinitionRepository.save(processDefinition);
        saveProcessSteps(savedDefinition, processDefinitionDTO);
    }

    public void saveProcessSteps(ProcessDefinition definition, ProcessDefinitionDTO processDefinitionDTO) {
        for (ProcessStepDTO stepDTO : processDefinitionDTO.getSteps()) {
            saveProcessStep(definition, stepDTO);
        }
    }

    private void saveProcessStep(ProcessDefinition definition, ProcessStepDTO stepDTO) {
        switch (stepDTO.getStepType()) {
            case "APPROVAL" -> saveApprovalSteps(definition, stepDTO);
            case "CONDITION" -> saveConditionSteps(definition, stepDTO);
            case "NOTIFY" -> saveNotificationSteps(definition, stepDTO);
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


    @Transactional
    public void updateProcessDefinition(ProcessDefinitionDTO dto) {
        ProcessDefinition definition = processDefinitionRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("ProcessDefinition not found"));

        definition.setName(dto.getName());
        definition.setFormSchemaId(dto.getFormTemplate().getId());

        Map<Long, ProcessStepDTO> incomingStepMap = dto.getSteps().stream()
                .filter(step -> step.getId() != null)
                .collect(Collectors.toMap(ProcessStepDTO::getId, step -> step));

        // Remove and update
        Iterator<ProcessStep> iterator = definition.getSteps().iterator();
        while (iterator.hasNext()) {
            ProcessStep existingStep = iterator.next();
            if (incomingStepMap.containsKey(existingStep.getId())) {
                updateExistingStep(existingStep, incomingStepMap.get(existingStep.getId()));
            } else {
                iterator.remove();
                processStepRepository.delete(existingStep); // Make sure it's gone from DB and memory
            }
        }

        // Add new steps
        dto.getSteps().stream()
                .filter(step -> step.getId() == null)
                .forEach(stepDTO -> {
                    String newStepName = stepDTO.getName();
                    boolean exists = definition.getSteps().stream()
                            .anyMatch(s -> s.getName().equalsIgnoreCase(newStepName));
                    if (!exists) {
                        ProcessStep newStep = mapper.convertStepDTOToEntity(stepDTO, definition);
                        definition.getSteps().add(newStep); // JPA will save with cascade
                    }
                });

        processDefinitionRepository.save(definition);
    }

    private void updateExistingStep(ProcessStep entity, ProcessStepDTO dto) {
        entity.setName(dto.getName());

        if (entity instanceof ApprovalStep approvalStep && dto.getStepType().equals("APPROVAL")) {
            approvalStep.setValidatorRoles(dto.getValidatorRoles());
            approvalStep.setRequiredApproval(dto.getRequiredApproval());
            approvalStep.setFormId(dto.getFormTemplate().getId());
        }

        if (entity instanceof NotificationStep notificationStep && dto.getStepType().equals("NOTIFY") ){
            notificationStep.setRecipients(dto.getRecipients());
            notificationStep.setMessage(dto.getMessage());
        }

        if (entity instanceof ConditionStep conditionStep && dto.getStepType().equals("CONDITION")) {
            // Supprimer anciennes conditions
            conditionStep.getConditions().clear();
            // Recréer les nouvelles conditions
            List<Condition> updatedConditions = dto.getCondition().stream()
                    .map(mapper::conditionDTOToEntity)
                    .peek(cond -> cond.setConditionStep(conditionStep))
                    .collect(Collectors.toList());

            conditionStep.getConditions().addAll(updatedConditions);
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

    public List<ProcessDefinitionDTO> getAllProcessDefinition() {
        return  processDefinitionRepository.findAll().stream().map(processDefinition->ProcessDefinitionDTO.builder()
                .id(processDefinition.getId())
                .formTemplate( FormTemplateDTO.builder()
                        .name(formSchemaRepository.findById(processDefinition.getFormSchemaId()).get().getName())
                        .id(processDefinition.getFormSchemaId())
                                .build())
                .name(processDefinition.getName())
                .steps(processDefinition.getSteps().stream()
                        .map(mapper::convertStepEntityToDTO)
                        .toList())
                .build()).toList();
    }


    public List<ReportDTO> getAllCurrentUserReport() {


        String currentUserId = keycloakSecurityUtil.getCurrentUserId();
        return processInstanceRepository.findByActorId(currentUserId).stream()
                .map(processInstance -> buildReportDTO(processInstance, processInstance.getProcessDefinition().getName()))
                .collect(Collectors.toList());
    }

    private ReportDTO buildReportDTO(ProcessInstance processInstance, String processDefinitionName) {
        return new ReportDTO.ReportDTOBuilder()
                .processDefinitionName(processInstance.getProcessDefinition().getName())
                .username(keycloakSecurityUtil.getUserNameById(processInstance.getActorId()))
                .currentStatus(processInstance.getStatus())
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

    public List<ReportDTO> getAllReports() {
        String processDefinitionName = processDefinitionRepository.findAll().stream()
                .findFirst()
                .map(ProcessDefinition::getName)
                .orElse("Unknown Process");



        return processInstanceRepository.findAll().stream()
                .map(processInstance -> buildReportDTO(processInstance, processDefinitionName))
                .collect(Collectors.toList());

    }

    public void saveAllProcessDefinition(List<ProcessDefinitionDTO> processDefinitionsDTO) {
        processDefinitionsDTO.forEach(this::saveProcessDefinition);
    }

    public void updateAllProcessDefinition(List<ProcessDefinitionDTO> processDefinitionsDTO) {
        processDefinitionsDTO.forEach(this::updateProcessDefinition);
    }

    public ProcessInstanceDTO getRequestById(Long id) {
        ProcessInstance processInstance = processInstanceRepository.findById(id).orElseThrow(()->new EntityNotFoundException("Process Instance not found: " + id));
        Long formSchemaId= processInstance.getProcessDefinition().getFormSchemaId();
        return mapper.processInstanceToDTO(processInstance, formSchemaId);
    }

    public ProcessDefinitionDTO getProcessDefinitionById(Long id) {
         ProcessDefinition processDefinition = processDefinitionRepository.findById(id).orElseThrow(()->
                 new EntityNotFoundException("Process Definition not found: " + id));
         return ProcessDefinitionDTO.builder()
                 .id(processDefinition.getId())
                 .formTemplate( FormTemplateDTO.builder()
                         .name(formSchemaRepository.findById(processDefinition.getFormSchemaId()).get().getName())
                         .id(processDefinition.getFormSchemaId())
                         .build())
                 .name(processDefinition.getName())
                 .steps(processDefinition.getSteps().stream()
                         .map(mapper::convertStepEntityToDTO)
                         .toList())
                 .build();


    }
}