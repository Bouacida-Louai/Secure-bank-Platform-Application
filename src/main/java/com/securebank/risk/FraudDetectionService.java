package com.securebank.risk;

import com.securebank.repositories.AccountRepository;
import com.securebank.repositories.TransactionRepository;
import com.securebank.transaction.TransactionEntity;
import com.securebank.transaction.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    // Thresholds
    private static final BigDecimal LARGE_AMOUNT_THRESHOLD =
            new BigDecimal("10000");
    private static final int MAX_TRANSACTIONS_PER_5_MIN = 3;
    private static final int UNUSUAL_HOUR_START = 23;
    private static final int UNUSUAL_HOUR_END = 5;

    // Check transaction for fraud after it's saved
    @Transactional
    public void analyzeTransaction(TransactionEntity transaction) {
        List<String> flags = new ArrayList<>();

        // Rule 1 — Large amount
        if (transaction.getAmount()
                .compareTo(LARGE_AMOUNT_THRESHOLD) > 0) {
            flags.add("LARGE_AMOUNT: " + transaction.getAmount());
        }

        // Rule 2 — Unusual hours (11pm - 5am)
        int hour = LocalDateTime.now().getHour();
        if (hour >= UNUSUAL_HOUR_START || hour <= UNUSUAL_HOUR_END) {
            flags.add("UNUSUAL_HOUR: " + hour + ":00");
        }

        // Rule 3 — Too many transactions in 5 minutes
        if (transaction.getFromAccount() != null) {
            LocalDateTime fiveMinutesAgo =
                    LocalDateTime.now().minusMinutes(5);
            List<TransactionEntity> recentTx = transactionRepository
                    .findRecentTransactionsByAccount(
                            transaction.getFromAccount().getId(),
                            fiveMinutesAgo
                    );

            if (recentTx.size() >= MAX_TRANSACTIONS_PER_5_MIN) {
                flags.add("HIGH_FREQUENCY: "
                        + recentTx.size() + " txns in 5 mins");
            }
        }

        // If any flags → mark as SUSPICIOUS
        if (!flags.isEmpty()) {
            transaction.setStatus(TransactionStatus.FLAGGED);
            transactionRepository.save(transaction);
            log.warn("Transaction {} FLAGGED: {}",
                    transaction.getId(), flags);
        }
    }

    // Get all flagged transactions
    public List<TransactionEntity> getFlaggedTransactions() {
        return transactionRepository
                .findAllByStatus(TransactionStatus.FLAGGED);
    }
}