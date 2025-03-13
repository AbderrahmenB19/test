package com.user19.pfe_testing.controller;

import com.user19.pfe_testing.dto.ProcessHistoryDTO;
import com.user19.pfe_testing.dto.SubmissionDTO;
import com.user19.pfe_testing.service.ProcessService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("user")
public class ClientController {
    private final ProcessService processService;
    @PostMapping
    public ResponseEntity<String> submit(@RequestBody SubmissionDTO submissionDTO) {
        processService.startProcess(submissionDTO);
        return ResponseEntity.ok("ur request submitted successfully");
    }
    @GetMapping("/process-History")
    public ResponseEntity<List<ProcessHistoryDTO>> getProcessHistory() {
        return ResponseEntity.ok(processService.getProcessHistory());
    }

}
