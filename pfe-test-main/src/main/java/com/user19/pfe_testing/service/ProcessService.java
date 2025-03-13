package com.user19.pfe_testing.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.user19.pfe_testing.dto.*;

import com.user19.pfe_testing.mapper.Mapper;
import com.user19.pfe_testing.model.*;
import com.user19.pfe_testing.model.enums.ProcessStatus;
import com.user19.pfe_testing.repository.*;
import com.user19.pfe_testing.util.KeycloakSecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcessService {
    private final ProcessDefinitionRepository processDefinitionRepository;
    private final FormSchemaRepository formSchemaRepository;
    private final ProcessStepRepository processStepRepository;
    private final Mapper mapper;
    private final KeycloakSecurityUtil keycloakSecurityUtil;
    private final ProcessInstanceRepository processInstanceRepository;
    private final ProcessHistoryRepository processHistoryRepository;
    private final EmailService emailService;


    public void startProcess(SubmissionDTO submissionDTO) {
        ProcessDefinition processDefinition = processDefinitionRepository.findByFormSchemaId(submissionDTO.formSchemaId()).orElseThrow(() -> new EntityNotFoundException("Process definition not found"));
        String currentUserId = keycloakSecurityUtil.getCurrentUserId();
        String currentStepName = processDefinition.getSteps().getFirst().getName();
        ProcessInstance processInstance = ProcessInstance.builder()
                .processDefinition(processDefinition).actorId(currentUserId)
                .status(ProcessStatus.PENDING)
                .currentStepName(currentStepName)
                .formData(submissionDTO.formData())
                .build();
        String processInstanceId = processInstanceRepository.save(processInstance).getId();
        ProcessHistory processHistory = ProcessHistory.builder().processInstance(processInstance).action("process started").timestamp(LocalDateTime.now()).actorId(currentUserId).build();
        processHistoryRepository.save(processHistory);
        moveNextStep(processInstanceId);

    }

    public void moveNextStep(String processInstanceId) {
        ProcessInstance processInstance = processInstanceRepository.findById(processInstanceId).orElseThrow(() -> new EntityNotFoundException("Process Instance not found: " + processInstanceId));
        String currentStepName = processInstance.getCurrentStepName();
        if (isLastStep(currentStepName, processInstanceId)) {
            String clientName = keycloakSecurityUtil.getCurrentUserName(processInstance.getActorId());
            String clientEmail = keycloakSecurityUtil.getCurrentUserEmail(processInstance.getActorId());
            emailService.notifyClient(clientEmail, clientName, "ur process accepted");
        }
        ProcessStep currentProcessStep = processStepRepository.findByName(currentStepName);
        if (currentProcessStep instanceof NotificationStep) {
            NotificationStep notificationStep = (NotificationStep) currentProcessStep;
            emailService.notifyValidators(notificationStep);
        }
        if (currentProcessStep instanceof ConditionStep) {
            ConditionStep conditionStep = (ConditionStep) currentProcessStep;
            List<Condition> conditions = conditionStep.getConditions();
            for (Condition condition : conditions) {
                if (evaluateCondition(processInstance.getFormData(), condition.getCondition())) {
                    updateCurrentStepNameOfProcessInstanceFromConditionStep(processInstanceId, condition.getTargetStep());
                    break;
                }
            }
            return;
        }
        updateCurrentStepNameOfProcessInstance(processInstanceId, currentProcessStep.getName());
    }

    private boolean evaluateCondition(String formData, String condition) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> dataMap = null;
        try {
            dataMap = objectMapper.readValue(formData, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            context.setVariable(entry.getKey(), entry.getValue());
        }
        return parser.parseExpression(condition).getValue(context, Boolean.class);
    }

    private void updateCurrentStepNameOfProcessInstanceFromConditionStep(String processInstanceId, String stepName) {
        ProcessInstance processInstance = processInstanceRepository.findById(processInstanceId).orElseThrow(() -> new EntityNotFoundException("Process Instance not found: " + processInstanceId)

        );
        List<ProcessStep> processSteps = processInstance.getProcessDefinition().getSteps();
        if (processSteps.indexOf(stepName) == -1) {
            throw new EntityNotFoundException("Process step not found: " + stepName);
        }

        processInstance.setCurrentStepName(stepName);
        processInstanceRepository.save(processInstance);


    }

    private boolean isLastStep(String currentStepName, String processInstanceId) {
        ProcessInstance processInstance = processInstanceRepository.findById(processInstanceId).orElseThrow(() -> new EntityNotFoundException("Process Instance not found: " + processInstanceId));
        return Objects.equals(processInstance.getProcessDefinition().getSteps().getLast().getName(), currentStepName);
    }


    private void updateCurrentStepNameOfProcessInstance(String processInstanceId, String prevStepName) {
        ProcessInstance processInstance = processInstanceRepository.findById(processInstanceId).orElseThrow(() -> new EntityNotFoundException("Process Instance not found: " + processInstanceId)

        );
        List<ProcessStep> processSteps = processInstance.getProcessDefinition().getSteps();
        ProcessStep prevProcessStep = processStepRepository.findByName(prevStepName);
        int prevProcessStepIndex = processSteps.indexOf(prevProcessStep);

        String currentStepName = processSteps.get(prevProcessStepIndex + 1).getName();
        processInstance.setCurrentStepName(currentStepName);
        processInstanceRepository.save(processInstance);


    }

    public void saveProcessDefinition(ProcessDefinitionDTO processDefinitionDTO, FormSchemaDTO formSchemaDTO) {
        FormSchema savedFormSchema = saveFormSchema(formSchemaDTO);

        ProcessDefinition processDefinitionSaved = processDefinitionRepository.save(ProcessDefinition.builder().name(processDefinitionDTO.getName()).formSchema(savedFormSchema).build());

        List<ProcessStep> steps = processDefinitionDTO.getSteps().stream().map(stepDTO -> mapper.convertStepDTOToEntity(stepDTO, processDefinitionSaved)).collect(Collectors.toList());

        processStepRepository.saveAll(steps);

    }

    public void updateProcessDefinition(ProcessDefinitionDTO processDefinitionDTO) {


        ProcessDefinition processDefinitionSaved = processDefinitionRepository.findById(processDefinitionDTO.getId()).orElseThrow(() -> new EntityNotFoundException("No process with Id" + processDefinitionDTO.getId()));
        processDefinitionSaved.setName(processDefinitionDTO.getName());
        List<ProcessStep> steps = processDefinitionDTO.getSteps().stream().map(stepDTO -> mapper.convertStepDTOToEntity(stepDTO, processDefinitionSaved)).collect(Collectors.toList());

        processStepRepository.saveAll(steps);

    }

    public FormSchema saveFormSchema(FormSchemaDTO formSchemaRequest) {
        FormSchema formschema = FormSchema.builder().jsonSchema(formSchemaRequest.getJsonSchema()).build();
        return formSchemaRepository.save(formschema);
    }

    public FormSchema updateFormSchema(FormSchemaDTO formSchemaRequest) {
        FormSchema formschema = formSchemaRepository.findById(formSchemaRequest.getId()).orElseThrow(() -> new EntityNotFoundException("No form schema with Id" + formSchemaRequest.getId()));
        formschema.setJsonSchema(formSchemaRequest.getJsonSchema());
        return formSchemaRepository.save(formschema);

    }
    public void addLogsToProcessHistory(ProcessInstance processInstance , String action,String actorId,String comment){

        ProcessHistory processHistory= ProcessHistory.builder()
                .processInstance(processInstance)
                .action(action)
                .actorId(actorId)
                .timestamp(LocalDateTime.now())
                .comments(comment)
                .build();
        processHistoryRepository.save(processHistory);


    }
    public ProcessDefinition getProcessDefinition() {
        ProcessDefinition processDefinition = processDefinitionRepository.findAll().getFirst();
        if(processDefinition == null) return null;
        return processDefinition;



    }
    public List<ProcessHistoryDTO> getProcessHistory(){
        String currentUserId= keycloakSecurityUtil.getCurrentUserId();
        ProcessInstance processInstance= processInstanceRepository.findByActorIdAndStatus(currentUserId,ProcessStatus.PENDING);
        return processInstance.getHistory().stream().map(
                mapper::processHistoryToDTO
                ).toList();
    }
    public List<RapportDTO> getAllRapportDTO(){
        String currentUserId= keycloakSecurityUtil.getCurrentUserId();
        List<ProcessInstance> processInstances= processInstanceRepository.findByActorId(currentUserId);
        List<RapportDTO>  rapportDTOs= new ArrayList<>();
        processInstances.stream().forEach(
                processInstance -> {
                    rapportDTOs.add(RapportDTO
                            .builder()
                                    .processInstanceId(processInstance.getId())
                                    .processHistoryDTOList(processInstance.getHistory().stream()
                                            .map(mapper::processHistoryToDTO).toList())
                            .build());
                }
        );
        return rapportDTOs;
    }



}
