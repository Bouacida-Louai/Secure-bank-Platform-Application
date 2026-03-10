package com.securebank.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {


    @GetMapping("/public/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("SecureBank API is running ✅");
    }

    @GetMapping("/private/test")
    public ResponseEntity<String> test(Authentication authentication) {
        return ResponseEntity.ok(
                "Welcome: " + authentication.getName() +
                        " | Roles: " + authentication.getAuthorities()
        );
    }

    @GetMapping("/private/teller-only")
    @PreAuthorize("hasRole('TELLER')")
    public ResponseEntity<String> tellerOnly() {
        return ResponseEntity.ok("Hello Teller ✅");
    }

}
