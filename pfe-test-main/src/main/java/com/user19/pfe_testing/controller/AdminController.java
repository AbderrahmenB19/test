package com.user19.pfe_testing.controller;

import com.user19.pfe_testing.dto.FormSchemaDTO;
import com.user19.pfe_testing.dto.ProcessDefinitionDTO;

import com.user19.pfe_testing.model.ProcessDefinition;
import com.user19.pfe_testing.service.ProcessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {

    private final ProcessService processService;

    @PostMapping("/process")
    public ResponseEntity<String> saveProcess(
            @Valid @RequestBody ProcessDefinitionDTO processDefinitionDTO,
            @Valid @RequestBody FormSchemaDTO formSchemaDTO) {
        processService.saveProcessDefinition(processDefinitionDTO, formSchemaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("Process and form schema saved successfully.");
    }

    @PutMapping("/form-schema")
    public ResponseEntity<String> updateFormSchema(
            @Valid @RequestBody FormSchemaDTO formSchemaDTO) {
        processService.updateFormSchema(formSchemaDTO);
        return ResponseEntity.ok("Form schema updated successfully.");
    }

    @GetMapping("/process-definition")
    public ResponseEntity<ProcessDefinition> getProcessDefinition() {
        ProcessDefinition response = processService.getProcessDefinition();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/process-definition")
    public ResponseEntity<String> updateProcessDefinition(
            @Valid @RequestBody ProcessDefinitionDTO processDefinitionDTO) {
        processService.updateProcessDefinition(processDefinitionDTO);
        return ResponseEntity.ok("Process definition updated successfully.");
    }
}