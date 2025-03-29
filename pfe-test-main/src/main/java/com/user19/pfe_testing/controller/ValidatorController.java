package com.user19.pfe_testing.controller;

import com.user19.pfe_testing.dto.ApproveDTO;
import com.user19.pfe_testing.dto.ProcessInstanceDTO;
import com.user19.pfe_testing.dto.RejectDTO;
import com.user19.pfe_testing.service.ProcessService;
import com.user19.pfe_testing.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ValidatorController {
    private final ValidationService validatorService;
    private final ProcessService processService;

    @GetMapping("/{status}")
    public ResponseEntity<List<ProcessInstanceDTO>> getRequestsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(validatorService.getRequestsByStatus(status));
    }
    @PostMapping("/{id}/approve")
    public ResponseEntity<String> approve(@PathVariable("id") String id) {
        validatorService.approve(id);
        return ResponseEntity.ok("Approved");
    }
    @PostMapping("/{id}/reject")
    public ResponseEntity<String> reject(@PathVariable("id") String id,@RequestBody Map<String, String> requestBody) {
        String comment = requestBody.get("comment");
        validatorService.reject(id, comment);
        return ResponseEntity.ok("rejected");
    }

}
