package com.securebank.risk;

import com.securebank.account.AccountService;
import com.securebank.repositories.AccountRepository;
import com.securebank.transaction.TransactionEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/risk")
@RequiredArgsConstructor
@Tag(name = "Risk", description = "Fraud detection and risk management — RISK_ANALYST only")
public class RiskController {

    private final FraudDetectionService fraudDetectionService;
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    @GetMapping("/flagged")
    @PreAuthorize("hasAnyRole('RISK_ANALYST','SUPER_ADMIN')")
    @Operation(
            summary = "Get flagged transactions",
            description = "RISK_ANALYST only — returns all transactions flagged as suspicious by fraud detection rules"
    )
    public ResponseEntity<?> getFlaggedTransactions() {
        List<TransactionEntity> flagged = fraudDetectionService
                .getFlaggedTransactions();

        List<Map<String, Object>> response = flagged.stream()
                .map(tx -> Map.of(
                        "id",     (Object) tx.getId(),
                        "amount", tx.getAmount(),
                        "type",   tx.getType(),
                        "status", tx.getStatus(),
                        "from",   tx.getFromAccount() != null ?
                                tx.getFromAccount().getAccountNumber() : "N/A",
                        "to",     tx.getToAccount() != null ?
                                tx.getToAccount().getAccountNumber() : "N/A",
                        "at",     tx.getCreatedAt()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }
}