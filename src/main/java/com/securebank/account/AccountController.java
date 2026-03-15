package com.securebank.account;

import com.securebank.dtos.AccountResponse;
import com.securebank.dtos.CreateAccountRequest;
import com.securebank.dtos.FreezeAccountRequest;
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
public class AccountController {

    private final AccountService accountService;

    // TELLER only — create account for a customer
    @PostMapping
    @PreAuthorize("@bankSecurity.can('account:create')")
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(accountService.createAccount(request));
    }

    // Owner, TELLER, AUDITOR, RISK_ANALYST
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    // CUSTOMER sees their own accounts
    @GetMapping("/my")
    public ResponseEntity<List<AccountResponse>> getMyAccounts() {
        return ResponseEntity.ok(accountService.getMyAccounts());
    }

    // RISK_ANALYST only — freeze account
    @PatchMapping("/{id}/freeze")
    @PreAuthorize("@bankSecurity.can('account:freeze')")
    public ResponseEntity<AccountResponse> freezeAccount(
            @PathVariable Long id,
            @Valid @RequestBody FreezeAccountRequest request) {
        return ResponseEntity.ok(
                accountService.freezeAccount(id, request));
    }

    // SUPER_ADMIN only — close account
    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AccountResponse> closeAccount(
            @PathVariable Long id) {
        return ResponseEntity.ok(accountService.closeAccount(id));
    }

    // AUDITOR + SUPER_ADMIN — see all accounts
    @GetMapping
    @PreAuthorize("hasAnyRole('AUDITOR', 'SUPER_ADMIN')")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }
}
