package com.user19.pfe_testing.controller;

import com.user19.pfe_testing.dto.FormSchemaDTO;
import com.user19.pfe_testing.dto.ProcessDefinitionDTO;
import com.user19.pfe_testing.model.ProcessDefinition;
import com.user19.pfe_testing.service.ProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;


@RequestMapping(("/admin"))
@RestController
@RequiredArgsConstructor
public class AdminController {
    private final ProcessService processService;
    @PostMapping
    public ResponseEntity<String> saveProcess(@RequestBody ProcessDefinitionDTO processDefinitionDTO, @RequestBody FormSchemaDTO formSchemaDTO) {
        processService.saveProcessDefinition(processDefinitionDTO,formSchemaDTO);
        return ResponseEntity.ok("success");
    }
    @PutMapping("/FormSchema")
    public ResponseEntity<String> updateFormSchema(@RequestBody FormSchemaDTO formSchemaDTO) {
        processService.updateFormSchema(formSchemaDTO);
        return ResponseEntity.ok("success");
    }
    @GetMapping("/getProcessDefinition")
    public ResponseEntity<ProcessDefinition> getProcessDefinition(){
        return ResponseEntity.ok(processService.getProcessDefinition());
    }
    @PutMapping("/updateProcessDefinition")
    public ResponseEntity<String> updateProcessDefinition(@RequestBody ProcessDefinitionDTO processDefinitionDTO){
        processService.updateProcessDefinition(processDefinitionDTO);
        return ResponseEntity.ok("success");
    }

}
