package com.securebank.permission;

import com.securebank.repositories.PermissionRepository;
import com.securebank.repositories.UserPermissionRepository;
import com.securebank.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {


        if (permissionRepository.count() > 0) return;

        log.info("Seeding permissions...");

        PermissionEntity accountCreate = save("account:create", "Open new account", null);
        PermissionEntity accountFreeze = save("account:freeze", "Freeze an account", null);
        PermissionEntity txDeposit = save("transaction:deposit", "Deposit money", null);
        PermissionEntity txWithdraw = save("transaction:withdraw", "Withdraw money", null);
        PermissionEntity txTransfer = save("transaction:transfer", "Transfer money", null);
        PermissionEntity loanApply = save("loan:apply", "Apply for a loan", null);
        PermissionEntity loanApprove = save("loan:approve", "Approve loan", new BigDecimal("50000"));
        PermissionEntity reportExport = save("report:export", "Export reports", null);

    }

        private PermissionEntity save(String code,
                String description,
                BigDecimal maxAmount) {
            PermissionEntity p = PermissionEntity.builder()
                    .code(code)
                    .description(description)
                    .maxAmount(maxAmount)
                    .build();
            return permissionRepository.save(p);
        }



}
