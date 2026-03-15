package com.securebank.transaction.controller;

import com.securebank.transaction.TransactionService;
import com.securebank.transaction.dto.DepositWithdrawRequest;
import com.securebank.transaction.dto.TransactionResponse;
import com.securebank.transaction.dto.TransferRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // TELLER only
    @PostMapping("/deposit")
    @PreAuthorize("@bankSecurity.can('transaction:deposit')")
    public ResponseEntity<TransactionResponse> deposit(
            @Valid @RequestBody DepositWithdrawRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionService.deposit(request));
    }

    // TELLER only
    @PostMapping("/withdraw")
    @PreAuthorize("@bankSecurity.can('transaction:withdraw')")
    public ResponseEntity<TransactionResponse> withdraw(
            @Valid @RequestBody DepositWithdrawRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionService.withdraw(request));
    }

    // CUSTOMER or TELLER
    @PostMapping("/transfer")
    @PreAuthorize("@bankSecurity.can('transaction:transfer')")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionService.transfer(request));
    }

    // Owner or AUDITOR
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponse>> getHistory(
            @PathVariable Long accountId) {
        return ResponseEntity.ok(
                transactionService.getTransactionHistory(accountId));
    }
}