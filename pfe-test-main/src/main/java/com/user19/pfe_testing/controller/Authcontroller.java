package com.user19.pfe_testing.controller;

import com.user19.pfe_testing.util.KeycloakSecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class Authcontroller {
    private final KeycloakSecurityUtil keycloakSecurityUtil;

    @GetMapping("/roles")
    public ResponseEntity<List<String>> getRoles(){
        List<String > roles =keycloakSecurityUtil.getAllRoles();
        return ResponseEntity.ok(roles);
    }
}
