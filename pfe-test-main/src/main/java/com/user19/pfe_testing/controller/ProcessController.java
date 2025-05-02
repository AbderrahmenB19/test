package com.user19.pfe_testing.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.user19.pfe_testing.dto.ProcessDefinitionDTO;
import com.user19.pfe_testing.dto.ReportDTO;
import com.user19.pfe_testing.repository.ProcessStepRepository;
import com.user19.pfe_testing.service.ProcessService;
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
    private final ProcessStepRepository processStepRepository;

    @GetMapping
    public ResponseEntity<List<ReportDTO>> getAllCurrentUserReport() {
        return ResponseEntity.ok(processService.getAllCurrentUserReport());
    }
    @GetMapping("/all")
    public ResponseEntity<List<ReportDTO>> getAllReports() {
        return ResponseEntity.ok(processService.getAllReports());
    }


    @PatchMapping("cancel-request/{id}")
    public ResponseEntity<String> cancelRequest(@PathVariable Long id) {
        processService.cancelRequest(id);
        return ResponseEntity.ok("ur request cancelled successfully");

    }
    @GetMapping("/process-definition")
    public ResponseEntity<List<ProcessDefinitionDTO>> getAllProcessDefinition() {
        List<ProcessDefinitionDTO> response = processService.getAllProcessDefinition();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/process-definition")
    public ResponseEntity<String> saveProcessDefinition(
             @RequestBody ProcessDefinitionDTO processDefinitionDTO) {
        processService.saveProcessDefinition(processDefinitionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("Process  saved successfully.");
    }
    @PostMapping("/process-definition/all")
    public ResponseEntity<String> saveAllProcessDefinition(
            @RequestBody List<ProcessDefinitionDTO> processDefinitionsDTO) {
        processService.saveAllProcessDefinition(processDefinitionsDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("Process  saved successfully.");
    }
    @PutMapping("/process-definition")
    public ResponseEntity<String> updateProcessDefinition(
             @RequestBody ProcessDefinitionDTO processDefinitionDTO) {
        processService.updateProcessDefinition(processDefinitionDTO);
        return ResponseEntity.status(HttpStatus.OK).body("Process  updated successfully.");
    }
    @PutMapping("/process-definition/all")
    public ResponseEntity<String> updateAllProcessDefinition(
            @RequestBody List<ProcessDefinitionDTO> processDefinitionsDTO) {
        processService.updateAllProcessDefinition(processDefinitionsDTO);
        return ResponseEntity.status(HttpStatus.OK).body("Process  updated successfully.");
    }
    @PutMapping("/clear")
    public ResponseEntity<String> clear() {
        processStepRepository.deleteAll();
        return ResponseEntity.status(HttpStatus.OK).body("Process deleted successfully.");
    }


}
