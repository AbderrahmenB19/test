package com.user19.pfe_testing.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.user19.pfe_testing.dto.*;
import com.user19.pfe_testing.mapper.Mapper;
import com.user19.pfe_testing.model.*;
import com.user19.pfe_testing.model.enums.ProcessStatus;
import com.user19.pfe_testing.repository.*;

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
    private final ConditionEval conditionEval;
    @Transactional
    public void startProcess(SubmissionDTO submissionDTO) {
        ProcessDefinition processDefinition = processDefinitionRepository.findByFormSchemaId(submissionDTO.formSchemaId())
                .orElseThrow(() -> new EntityNotFoundException("Process definition not found"));

        String currentUserId = keycloakSecurityUtil.getCurrentUserId();
        String currentStepName = processDefinition.getSteps().getFirst().getName();

        ProcessInstance processInstance = ProcessInstance.builder()
                .processDefinition(processDefinition)
                .actorId(currentUserId)
                .status(ProcessStatus.PENDING)
                .currentStepName(currentStepName)
                .formData(submissionDTO.formData())
                .build();

        ProcessInstance savedProcessInstance =processInstanceRepository.save(processInstance);
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

        if (currentProcessStep instanceof NotificationStep) {
            handleNotificationStep((NotificationStep) currentProcessStep);
        }
        else if (currentProcessStep instanceof ConditionStep) {
            handleConditionStep((ConditionStep) currentProcessStep, processInstance);
        }
        else if (currentProcessStep instanceof ApprovalStep) {
            handleApprovalStep((ApprovalStep) currentProcessStep);
        }

    }


    public void moveNextStep(Long processInstanceId) {
        ProcessInstance processInstance = getProcessInstanceById(processInstanceId);
        System.out.println("uuuuuuuuuuuuu-id ");
        String currentStepName = processInstance.getCurrentStepName();
        System.out.println(currentStepName);

        if (isLastStep(currentStepName, processInstance)) {
            handleLastStep(processInstance);
            return;
        }

        ProcessStep currentProcessStep = processStepRepository.findByName(currentStepName);

        if (currentProcessStep instanceof NotificationStep) {
            handleNotificationStep((NotificationStep) currentProcessStep);
        }
        else if (currentProcessStep instanceof ConditionStep) {
            handleConditionStep((ConditionStep) currentProcessStep, processInstance);
            return;
        }
        else if (currentProcessStep instanceof ApprovalStep) {
            handleApprovalStep((ApprovalStep) currentProcessStep);
        }

        updateToNextStep(processInstance, currentProcessStep.getName());
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
            if (conditionEval.evaluateCondition(processInstance.getFormData(), condition.getCondition())) {
                updateCurrentStepFromCondition(processInstance.getId(), condition.getTargetStep());
                break;
            }
        }
    }

    private void handleApprovalStep(ApprovalStep approvalStep) {
        emailService.notifyValidators(approvalStep);
    }

    private boolean evaluateCondition(String formData, String condition) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> dataMap = objectMapper.readValue(formData, new TypeReference<Map<String, Object>>() {});

            // Create a type converter to handle number/string comparisons
            StandardTypeConverter typeConverter = new StandardTypeConverter(new DefaultConversionService());

            ExpressionParser parser = new SpelExpressionParser();
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setTypeConverter(typeConverter);
            context.setVariable("formData", dataMap);
            System.out.println(formData);
            System.out.println(condition);// Add the entire map as a variable

            // Parse and evaluate the expression
            System.out.println(context);
            System.out.println(parser.parseExpression(condition).getValue(context, Boolean.class));
            return parser.parseExpression(condition).getValue(context, Boolean.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to evaluate condition: " + condition, e);
        }
    }

    private void updateCurrentStepFromCondition(Long processInstanceId, String stepName) {
        ProcessInstance processInstance = getProcessInstanceById(processInstanceId);
        validateStepExists(processInstance, stepName);
        //System.out.println("sssssssssssssssssssssssssssssssssssss");
        //System.out.println(processInstance);
        //System.out.println(stepName);
        //System.out.println("updateeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
        processInstance.setCurrentStepName(stepName);
        System.out.println("updateeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
        ProcessInstance Repo = processInstanceRepository.save(processInstance);
        System.out.println(Repo.getId());
        System.out.println("updateeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
        System.out.println(Repo.getCurrentStepName());
    }

    private boolean isLastStep(String currentStepName, ProcessInstance processInstance) {
        return Objects.equals(processInstance.getProcessDefinition().getSteps().getLast().getName(), currentStepName);
    }

    private void updateToNextStep(ProcessInstance processInstance, String prevStepName) {
        List<ProcessStep> processSteps = processInstance.getProcessDefinition().getSteps();
        int nextStepIndex = processSteps.indexOf(processStepRepository.findByName(prevStepName)) + 1;

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
        for(ProcessStepDTO stepDTO : processDefinitionDTO.getSteps()) {
            System.out.println("counterrrrr");
            if(Objects.equals(stepDTO.getStepType(), "APPROVAL")) {
                System.out.println("AAAAAAAAAAAA");
                saveApprovalSteps(definition,stepDTO);
            }
            if(Objects.equals(stepDTO.getStepType(), "CONDITION")) {
                saveConditionSteps(definition,stepDTO);
            }
            if(Objects.equals(stepDTO.getStepType(), "NOTIFY")) {
                saveNotificationSteps(definition,stepDTO);
            }
        }
        System.out.println("finssssssssssssss,k,kk,k,");

    }

    private void saveNotificationSteps(ProcessDefinition definition, ProcessStepDTO stepDTO) {
        Long id = notificationStepRepository.save((NotificationStep) mapper.convertStepDTOToEntity(stepDTO,definition)).getId();
        System.out.println(id);
    }

    private void saveApprovalSteps(ProcessDefinition definition, ProcessStepDTO stepDTO) {
         Long id =approvalStepRepository.save((ApprovalStep) mapper.convertStepDTOToEntity(stepDTO,definition)).getId();
        System.out.println("iddddddddddddddddddddddddddddd hahiiiiiiiiiii");
        System.out.println(id);


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

        // Clear and save in the same transaction
        clearAllProcess();
        processDefinition.setName(processDefinitionDTO.getName());
        ProcessDefinition savedProcessDefinition = processDefinitionRepository.save(processDefinition);

        saveProcessSteps(savedProcessDefinition, processDefinitionDTO);
        updateCurrentStepOfProcessInstance(savedProcessDefinition);
    }

    private void clearAllProcess() {
        processStepRepository.deleteAll();
        approvalStepRepository.deleteAll();
        conditionStepRepository.deleteAll();
        notificationStepRepository.deleteAll();
    }

    private void updateCurrentStepOfProcessInstance(ProcessDefinition processDefinition) {
        List<ProcessInstance> filteredProcessInstance = filterProcessInstanceByNotExistingSteps(processDefinition);
        if ((long) filteredProcessInstance.size() > 0) {
            for (ProcessInstance process : filteredProcessInstance) {
                List<ProcessHistory> processHistories= process.getHistory();
                String lastCurrentExistingStepInHistories=getLastExistingStepInHistory(processHistories, processDefinition);
                System.out.println(lastCurrentExistingStepInHistories);
                updateToNextStep(process,lastCurrentExistingStepInHistories);

            }

        }
    }

    private String getLastExistingStepInHistory(List<ProcessHistory> processHistories,
                                                ProcessDefinition processDefinition) {
        // Get steps from the processDefinition (not from repository)
        List<String> stepsName = processDefinition.getSteps()
                .stream()
                .map(ProcessStep::getName)
                .toList();

        System.out.println("Available step names: " + stepsName);

        // Find the last valid step from history
        for(int i = processHistories.size()-1; i >= 0; i--) {
            String action = processHistories.get(i).getAction();
            if(stepsName.contains(action)) {
                return action;
            }
        }

        // Default to first step if none found
        return stepsName.isEmpty() ? null : stepsName.get(0);
    }

    private List<ProcessInstance> filterProcessInstanceByNotExistingSteps(ProcessDefinition processDefinition) {
        System.out.println("*********************");
        List<String> stepsName= processStepRepository.findAll().stream().map(ProcessStep::getName).toList();
        System.out.println(1);
        System.out.println(stepsName);
        List<ProcessInstance> filteredProcessInstances= processInstanceRepository.findAll().stream()
                .filter(p->{
                    System.out.println(p.getCurrentStepName());
                    System.out.println(!stepsName.contains(p.getCurrentStepName()));
                    return !stepsName.contains(p.getCurrentStepName());
                })
                .toList();
        System.out.println(filteredProcessInstances);
        return filteredProcessInstances;
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
                        .toList()
                )

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

    public List<RapportDTO> getAllRapports() {
        String processDefinitionName = processDefinitionRepository.findAll().stream()
                .findFirst()
                .map(ProcessDefinition::getName)
                .orElse("Unknown Process");

        String currentUserId = keycloakSecurityUtil.getCurrentUserId();
        Optional<List<ProcessInstance>> processInstanceList= Optional.ofNullable(processInstanceRepository.findByActorId(currentUserId));

        return processInstanceRepository.findByActorId(currentUserId).stream()
                .map(processInstance -> buildRapportDTO(processInstance, processDefinitionName))
                .collect(Collectors.toList());
    }

    private RapportDTO buildRapportDTO(ProcessInstance processInstance, String processDefinitionName) {
        return new RapportDTO.RapportDTOBuilder()
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