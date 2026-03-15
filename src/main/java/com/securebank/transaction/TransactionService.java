package com.securebank.transaction;

import com.securebank.account.AccountEntity;
import com.securebank.audit.service.AuditService;
import com.securebank.permission.UserSyncService;
import com.securebank.repositories.AccountRepository;
import com.securebank.repositories.TransactionRepository;
import com.securebank.transaction.dto.DepositWithdrawRequest;
import com.securebank.transaction.dto.TransactionResponse;
import com.securebank.transaction.dto.TransferRequest;
import com.securebank.user.UserEntity;
import com.securebank.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserSyncService userSyncService;
    private final AuditService auditService;

    private TransactionResponse toResponse(TransactionEntity tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .fromAccountNumber(tx.getFromAccount() != null ?
                        tx.getFromAccount().getAccountNumber() : null)
                .toAccountNumber(tx.getToAccount() != null ?
                        tx.getToAccount().getAccountNumber() : null)
                .amount(tx.getAmount())
                .type(tx.getType())
                .status(tx.getStatus())
                .initiatedBy(tx.getInitiatedBy().getFullName())
                .description(tx.getDescription())
                .createdAt(tx.getCreatedAt())
                .build();
    }

    // TELLER deposits money into account
    @Transactional
    public TransactionResponse deposit(DepositWithdrawRequest request) {
        AccountEntity account = accountRepository
                .findByIdWithLock(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Save balance before for audit
        BigDecimal balanceBefore = account.getBalance();

        account.credit(request.getAmount());
        accountRepository.save(account);

        UserEntity teller = userSyncService.syncUser();

        TransactionEntity tx = TransactionEntity.builder()
                .toAccount(account)
                .amount(request.getAmount())
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.COMPLETED)
                .initiatedBy(teller)
                .description(request.getDescription())
                .build();

        TransactionEntity saved = transactionRepository.save(tx);

        // Audit log
        auditService.log("DEPOSIT", "Account", account.getId(),
                Map.of("balance", balanceBefore),
                Map.of("balance", account.getBalance())
        );

        return toResponse(saved);
    }

    // TELLER withdraws money from account
    @Transactional
    public TransactionResponse withdraw(DepositWithdrawRequest request) {
        AccountEntity account = accountRepository
                .findByIdWithLock(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        BigDecimal balanceBefore = account.getBalance();

        // debit() checks frozen + insufficient funds
        account.debit(request.getAmount());
        accountRepository.save(account);

        UserEntity teller = userSyncService.syncUser();

        TransactionEntity tx = TransactionEntity.builder()
                .fromAccount(account)
                .amount(request.getAmount())
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.COMPLETED)
                .initiatedBy(teller)
                .description(request.getDescription())
                .build();

        TransactionEntity saved = transactionRepository.save(tx);

        auditService.log("WITHDRAWAL", "Account", account.getId(),
                Map.of("balance", balanceBefore),
                Map.of("balance", account.getBalance())
        );

        return toResponse(saved);
    }

    // CUSTOMER or TELLER transfers between accounts
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TransactionResponse transfer(TransferRequest request) {
        if (!request.getFromAccountId().equals(request.getToAccountId())) {// Lock BOTH accounts — always in same order to prevent deadlock
            Long firstId = Math.min(
                    request.getFromAccountId(), request.getToAccountId());
            Long secondId = Math.max(
                    request.getFromAccountId(), request.getToAccountId());

            AccountEntity first = accountRepository
                    .findByIdWithLock(firstId)
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            AccountEntity second = accountRepository
                    .findByIdWithLock(secondId)
                    .orElseThrow(() -> new RuntimeException("Account not found"));

            // Identify which is from and which is to
            AccountEntity fromAccount = first.getId()
                    .equals(request.getFromAccountId()) ? first : second;
            AccountEntity toAccount = first.getId()
                    .equals(request.getToAccountId()) ? first : second;

            // Check ownership for CUSTOMER
            UserEntity currentUser = userSyncService.syncUser();
            if (currentUser.getRole() == UserRole.CUSTOMER) {
                if (!fromAccount.getOwner().getId()
                        .equals(currentUser.getId())) {
                    throw new RuntimeException(
                            "You can only transfer from your own account");
                }
            }

            BigDecimal fromBefore = fromAccount.getBalance();
            BigDecimal toBefore = toAccount.getBalance();

            fromAccount.debit(request.getAmount());
            toAccount.credit(request.getAmount());

            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            TransactionEntity tx = TransactionEntity.builder()
                    .fromAccount(fromAccount)
                    .toAccount(toAccount)
                    .amount(request.getAmount())
                    .type(TransactionType.TRANSFER)
                    .status(TransactionStatus.COMPLETED)
                    .initiatedBy(currentUser)
                    .description(request.getDescription())
                    .build();

            TransactionEntity saved = transactionRepository.save(tx);

            auditService.log("TRANSFER", "Account",
                    fromAccount.getId(),
                    Map.of("fromBalance", fromBefore,
                            "toBalance", toBefore),
                    Map.of("fromBalance", fromAccount.getBalance(),
                            "toBalance", toAccount.getBalance())
            );

            return toResponse(saved);
        } else {
            throw new RuntimeException(
                    "Cannot transfer to the same account");
        }

    }

    // Get transaction history for an account
    public List<TransactionResponse> getTransactionHistory(Long accountId) {
        return transactionRepository
                .findTransactionHistory(accountId)
                .stream()
                .map(this::toResponse)
                .toList();
    }
}