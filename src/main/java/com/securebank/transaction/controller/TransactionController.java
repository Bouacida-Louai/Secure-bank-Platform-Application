package com.securebank.transaction.controller;

import com.securebank.transaction.TransactionService;
import com.securebank.transaction.dto.DepositWithdrawRequest;
import com.securebank.transaction.dto.TransactionResponse;
import com.securebank.transaction.dto.TransferRequest;
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
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Financial operations — deposits, withdrawals, transfers")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    @PreAuthorize("@bankSecurity.can('transaction:deposit')")
    @Operation(
            summary = "Deposit money",
            description = "TELLER only — deposit cash into a customer account"
    )
    public ResponseEntity<TransactionResponse> deposit(
            @Valid @RequestBody DepositWithdrawRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionService.deposit(request));
    }

    @PostMapping("/withdraw")
    @PreAuthorize("@bankSecurity.can('transaction:withdraw')")
    @Operation(
            summary = "Withdraw money",
            description = "TELLER only — withdraw cash from a customer account"
    )
    public ResponseEntity<TransactionResponse> withdraw(
            @Valid @RequestBody DepositWithdrawRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionService.withdraw(request));
    }

    @PostMapping("/transfer")
    @PreAuthorize("@bankSecurity.can('transaction:transfer')")
    @Operation(
            summary = "Transfer money",
            description = "CUSTOMER or TELLER — transfer between accounts. CUSTOMER can only transfer from their own accounts"
    )
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionService.transfer(request));
    }

    @GetMapping("/account/{accountId}")
    @Operation(
            summary = "Get transaction history",
            description = "Returns all transactions for a specific account — owner or AUDITOR"
    )
    public ResponseEntity<List<TransactionResponse>> getHistory(
            @PathVariable Long accountId) {
        return ResponseEntity.ok(
                transactionService.getTransactionHistory(accountId));
    }
}