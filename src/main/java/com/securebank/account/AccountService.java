package com.securebank.account;

import com.securebank.dtos.AccountResponse;
import com.securebank.dtos.CreateAccountRequest;
import com.securebank.dtos.FreezeAccountRequest;
import com.securebank.permission.UserSyncService;
import com.securebank.repositories.AccountRepository;
import com.securebank.repositories.UserRepository;
import com.securebank.user.UserEntity;
import com.securebank.user.UserRole;
import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j@Builder

public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final UserSyncService userSyncService;

    private String generateAccountNumber() {
        return "SB" + System.currentTimeMillis();
    }

    private AccountResponse toResponse(AccountEntity account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .type(account.getType())
                .status(account.getStatus())
                .ownerName(account.getOwner().getFullName())
                .ownerEmail(account.getOwner().getEmail())
                .createdAt(account.getCreatedAt())
                .build();
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        UserEntity owner=userRepository.findById(request.getOwnerId())
                .orElseThrow(()->new RuntimeException("USER NOT FOUND"));

        AccountEntity account = AccountEntity.builder()
                .accountNumber(generateAccountNumber())
                .balance(request.getInitialDeposit())
                .type(request.getType())
                .status(AccountStatus.ACTIVE)
                .owner(owner)
                .build();

        AccountEntity saved = accountRepository.save(account);
        log.info("Account created: {} for user: {}",
                saved.getAccountNumber(), owner.getEmail());
        return toResponse(saved);

    }


    public AccountResponse getAccountById(Long id) {
        AccountEntity account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        UserEntity currentUser = userSyncService.syncUser();
        boolean isOwner = account.getOwner().getId()
                .equals(currentUser.getId());
        boolean isPrivileged = currentUser.getRole() == UserRole.TELLER
                || currentUser.getRole() == UserRole.AUDITOR
                || currentUser.getRole() == UserRole.RISK_ANALYST
                || currentUser.getRole() == UserRole.SUPER_ADMIN;

        if (!isOwner && !isPrivileged) {
            throw new RuntimeException("Access denied");
        }
        return toResponse(account);

    }

    public List<AccountResponse> getMyAccounts(){
        UserEntity currentUser =userSyncService.syncUser();
        return accountRepository.findAllByOwnerId(currentUser.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AccountResponse freezeAccount(Long id, FreezeAccountRequest request) {
        AccountEntity account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new RuntimeException("Account is already frozen");
        }

        account.setStatus(AccountStatus.FROZEN);
        log.info("Account {} frozen. Reason: {}",
                account.getAccountNumber(), request.getReason());
        return toResponse(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse closeAccount(Long id) {
        AccountEntity account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException(
                    "Cannot close account with positive balance");
        }

        account.setStatus(AccountStatus.CLOSED);
        log.info("Account {} closed", account.getAccountNumber());
        return toResponse(accountRepository.save(account));
    }


    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }





}
