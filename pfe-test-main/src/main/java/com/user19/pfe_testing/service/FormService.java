package com.user19.pfe_testing.service;

import com.user19.pfe_testing.dto.FormSchemaDTO;
import com.user19.pfe_testing.mapper.Mapper;
import com.user19.pfe_testing.model.FormSchema;
import com.user19.pfe_testing.model.ProcessDefinition;
import com.user19.pfe_testing.repository.FormSchemaRepository;
import com.user19.pfe_testing.repository.ProcessDefinitionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FormService {
    private final FormSchemaRepository formSchemaRepository;
    private final Mapper mapper;
    private final ProcessDefinitionRepository processDefinitionRepository;

    public void saveFormSchema(FormSchemaDTO formSchemaRequest) {
        if (formSchemaRepository.count() > 0) {
            throw new IllegalStateException("A form schema already exists. Use update instead.");
        }
        ProcessDefinition processDefinition = processDefinitionRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("No process definition found. Please create a process first."));

        FormSchema formSchema = FormSchema.builder()
                .jsonSchema(formSchemaRequest.getJsonSchema())
                .processDefinition(processDefinition)
                .build();
        formSchemaRepository.save(formSchema);
    }

    public void updateFormSchema(FormSchemaDTO formSchemaRequest) {
        FormSchema formSchema = formSchemaRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("No form schema found to update."));

        formSchema.setJsonSchema(formSchemaRequest.getJsonSchema());
        formSchemaRepository.save(formSchema);
    }

    public FormSchemaDTO getFormSchema(){
        return mapper.formSchemaToDTO(formSchemaRepository.findAll().getFirst());
    }

}
