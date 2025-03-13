package com.user19.pfe_testing.controller;

import com.user19.pfe_testing.dto.ApproveDTO;
import com.user19.pfe_testing.dto.ProcessInstanceDTO;
import com.user19.pfe_testing.dto.RejectDTO;
import com.user19.pfe_testing.model.ProcessInstance;
import com.user19.pfe_testing.service.ProcessService;
import com.user19.pfe_testing.service.ValidatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/validator")
@RequiredArgsConstructor
public class ValidatorController {
    private final ValidatorService validatorService;
    private final ProcessService processService;

    @GetMapping
    public ResponseEntity<List<ProcessInstanceDTO>> getALlPendingProcessInstances() {
        return ResponseEntity.ok(validatorService.getAllPendingProcess());
    }
    @PostMapping("/Approve")
    public ResponseEntity<String> Approve(@RequestBody ApproveDTO approveDTO) {
        validatorService.Approve(approveDTO.processInstanceId(), approveDTO.comment());
        return ResponseEntity.ok("Approved");
    }
    @PostMapping("/reject")
    public ResponseEntity<String> reject(@RequestBody RejectDTO rejectDTO) {
        validatorService.reject(rejectDTO.processInstanceId(), rejectDTO.comment());
        return ResponseEntity.ok("rejected");
    }

}
