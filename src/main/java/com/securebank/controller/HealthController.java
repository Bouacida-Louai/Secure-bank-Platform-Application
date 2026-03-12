package com.securebank.controller;

import com.securebank.permission.UserSyncService;
import com.securebank.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthController {


    private final UserSyncService userSyncService;

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
    @GetMapping("/private/me")
    public ResponseEntity<?> me() {
        UserEntity user = userSyncService.syncUser();
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "keycloakId", user.getKeycloakId()
        ));
    }


    // Only LOAN_OFFICER can access — with amount limit
    @GetMapping("/private/loan-test")
    @PreAuthorize("@bankSecurity.can('loan:approve', #amount)")
    public ResponseEntity<String> loanTest(
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok("Loan of $" + amount + " approved ✅");
    }

    // Only RISK_ANALYST can access
    @GetMapping("/private/risk-only")
    @PreAuthorize("@bankSecurity.can('account:freeze')")
    public ResponseEntity<String> riskOnly() {
        return ResponseEntity.ok("Hello Risk Analyst ✅");
    }

}
