package com.securebank.controller;

import com.securebank.permission.UserSyncService;
import com.securebank.user.UserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Health check and test endpoints")
public class HealthController {

    private final UserSyncService userSyncService;

    @GetMapping("/public/health")
    @Operation(
            summary = "Health check",
            description = "Public endpoint — checks if the API is running"
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("SecureBank API is running ✅");
    }

    @GetMapping("/private/test")
    @Operation(
            summary = "Auth test",
            description = "Returns current user name and roles from JWT"
    )
    public ResponseEntity<String> test(Authentication authentication) {
        return ResponseEntity.ok(
                "Welcome: " + authentication.getName() +
                        " | Roles: " + authentication.getAuthorities()
        );
    }

    @GetMapping("/private/me")
    @Operation(
            summary = "Get current user",
            description = "Returns the current authenticated user's profile from DB"
    )
    public ResponseEntity<?> me() {
        UserEntity user = userSyncService.syncUser();
        return ResponseEntity.ok(Map.of(
                "id",         user.getId(),
                "fullName",   user.getFullName(),
                "email",      user.getEmail(),
                "role",       user.getRole(),
                "keycloakId", user.getKeycloakId()
        ));
    }

    @GetMapping("/private/teller-only")
    @PreAuthorize("hasRole('TELLER')")
    @Operation(
            summary = "Teller test",
            description = "TELLER only — test endpoint"
    )
    public ResponseEntity<String> tellerOnly() {
        return ResponseEntity.ok("Hello Teller ✅");
    }

    @GetMapping("/private/loan-test")
    @PreAuthorize("@bankSecurity.can('loan:approve', #amount)")
    @Operation(
            summary = "Loan approval test",
            description = "LOAN_OFFICER only — tests amount-aware permission"
    )
    public ResponseEntity<String> loanTest(
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok("Loan of $" + amount + " approved ✅");
    }

    @GetMapping("/private/risk-only")
    @PreAuthorize("@bankSecurity.can('account:freeze')")
    @Operation(
            summary = "Risk analyst test",
            description = "RISK_ANALYST only — test endpoint"
    )
    public ResponseEntity<String> riskOnly() {
        return ResponseEntity.ok("Hello Risk Analyst ✅");
    }
}