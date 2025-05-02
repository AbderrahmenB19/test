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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FormService {
    private final FormSchemaRepository formSchemaRepository;
    private final Mapper mapper;
    private final ProcessDefinitionRepository processDefinitionRepository;
    public void saveFormSchema(FormSchemaDTO formSchemaRequest) {
        FormSchema formSchema = FormSchema.builder()
                .name(formSchemaRequest.getName())
                .description(formSchemaRequest.getDescription())
                .jsonSchema(formSchemaRequest.getJsonSchema())
                .lastUpdate(LocalDateTime.now())
                .build();
        formSchemaRepository.save(formSchema);
    }

    public void updateFormSchema(FormSchemaDTO formSchemaRequest) {
        FormSchema formSchema = formSchemaRepository.findById(formSchemaRequest.getId())
                .orElseThrow(() -> new EntityNotFoundException("No form schema found to update."));
        formSchema.setJsonSchema(formSchemaRequest.getJsonSchema());
        formSchema.setDescription(formSchemaRequest.getDescription());
        formSchema.setName(formSchemaRequest.getName());
        formSchemaRepository.save(formSchema);
    }

    public FormSchemaDTO getFormSchema(Long id ){
        return  formSchemaRepository.findById(id).map(mapper::formSchemaToDTO).orElseThrow(()->new RuntimeException("Entity Not found "));
    }

    public List<FormSchemaDTO> getAllFormSchema() {
        return formSchemaRepository.findAll().stream().map(mapper::formSchemaToDTO).toList();
    }
}
