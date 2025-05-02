package com.user19.pfe_testing.controller;

import com.user19.pfe_testing.dto.FormSchemaDTO;
import com.user19.pfe_testing.dto.SubmissionDTO;
import com.user19.pfe_testing.service.FormService;
import com.user19.pfe_testing.service.ProcessService;
import com.user19.pfe_testing.util.KeycloakSecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/form")
@RequiredArgsConstructor

public class FormController {
    private final ProcessService processService;
    private final FormService formService;
    private final  KeycloakSecurityUtil keycloakSecurityUtil;


    @GetMapping("/form-schema/{id}")
    public ResponseEntity<FormSchemaDTO> getFormSchema(@PathVariable("id") Long id ) {
        System.out.println(keycloakSecurityUtil.getCurrentUserRoles());
        System.out.println(keycloakSecurityUtil.getValidatorsEmailsByRoles(Set.of("MANAGER")));
        return ResponseEntity.ok(formService.getFormSchema(id));

    }
    @GetMapping("/form-schema")
    public ResponseEntity<List  <FormSchemaDTO>> getAllFormSchema() {
        System.out.println(keycloakSecurityUtil.getCurrentUserRoles());

        return ResponseEntity.ok(formService.getAllFormSchema());

    }
    @PostMapping("/form-schema")
    public ResponseEntity<String> saveFormSchema(@RequestBody FormSchemaDTO formSchemaDTO) {
        formService.saveFormSchema(formSchemaDTO);
        return ResponseEntity.ok("formschema saved");

    }

    @PostMapping
    public ResponseEntity<String> submit(@RequestBody SubmissionDTO submissionDTO) {
        processService.startProcess(submissionDTO);
        return ResponseEntity.ok("ur request submitted successfully");
    }
    @PutMapping("/form-schema")
    public ResponseEntity<String> updateFormSchema(
            @RequestBody FormSchemaDTO formSchemaDTO) {
        formService.updateFormSchema(formSchemaDTO);
        System.out.println(formSchemaDTO.getJsonSchema());
        return ResponseEntity.ok("Form schema updated successfully.");
    }



}
