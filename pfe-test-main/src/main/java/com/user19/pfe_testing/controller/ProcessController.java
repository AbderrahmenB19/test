package com.user19.pfe_testing.controller;

import com.user19.pfe_testing.dto.FormSchemaDTO;
import com.user19.pfe_testing.dto.ProcessDefinitionDTO;
import com.user19.pfe_testing.dto.RapportDTO;
import com.user19.pfe_testing.dto.SubmissionDTO;
import com.user19.pfe_testing.model.ProcessDefinition;
import com.user19.pfe_testing.service.ProcessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/processes")
@RequiredArgsConstructor
public class ProcessController {
    private final ProcessService processService;
    @GetMapping
    public ResponseEntity<List<RapportDTO>> getReports() {
        return ResponseEntity.ok(processService.getAllRapportDTO());
    }
    @PatchMapping("cancel-request/{id}")
    public ResponseEntity<String> cancelRequest(@PathVariable String id) {
        processService.cancelRequest(id);
        return ResponseEntity.ok("ur request cancelled successfully");

    }
    @GetMapping("/process-definition")
    public ResponseEntity<ProcessDefinition> getProcessDefinition() {
        ProcessDefinition response = processService.getProcessDefinition();
        return ResponseEntity.ok(response);
    }
    @PostMapping("/process-definition")
    public ResponseEntity<String> saveProcessDefinition(
             @RequestBody ProcessDefinitionDTO processDefinitionDTO) {
        processService.saveProcessDefinition(processDefinitionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("Process  saved successfully.");
    }
    @PutMapping("/process-definition")
    public ResponseEntity<String> updateProcessDefinition(
             @RequestBody ProcessDefinitionDTO processDefinitionDTO) {
        processService.updateProcessDefinition(processDefinitionDTO);
        return ResponseEntity.status(HttpStatus.OK).body("Process  updated successfully.");
    }


}
