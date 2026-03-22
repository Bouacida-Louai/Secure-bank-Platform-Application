package com.securebank.account.controller;

import com.securebank.account.AccountService;
import com.securebank.dtos.AccountResponse;
import com.securebank.dtos.CreateAccountRequest;
import com.securebank.dtos.FreezeAccountRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Account management endpoints")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @PreAuthorize("@bankSecurity.can('account:create')")
    @Operation(
            summary = "Create account",
            description = "TELLER only — opens a new bank account for a customer"
    )
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(accountService.createAccount(request));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get account by ID",
            description = "Accessible by account owner, TELLER, AUDITOR, or RISK_ANALYST"
    )
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @GetMapping("/my")
    @Operation(
            summary = "Get my accounts",
            description = "CUSTOMER sees only their own accounts"
    )
    public ResponseEntity<List<AccountResponse>> getMyAccounts() {
        return ResponseEntity.ok(accountService.getMyAccounts());
    }

    @PatchMapping("/{id}/freeze")
    @PreAuthorize("@bankSecurity.can('account:freeze')")
    @Operation(
            summary = "Freeze account",
            description = "RISK_ANALYST only — freeze a suspicious account with a reason"
    )
    public ResponseEntity<AccountResponse> freezeAccount(
            @PathVariable Long id,
            @Valid @RequestBody FreezeAccountRequest request) {
        return ResponseEntity.ok(
                accountService.freezeAccount(id, request));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Close account",
            description = "SUPER_ADMIN only — permanently close an account with zero balance"
    )
    public ResponseEntity<AccountResponse> closeAccount(
            @PathVariable Long id) {
        return ResponseEntity.ok(accountService.closeAccount(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AUDITOR', 'SUPER_ADMIN')")
    @Operation(
            summary = "Get all accounts",
            description = "AUDITOR and SUPER_ADMIN only — view all accounts in the system"
    )
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }
}