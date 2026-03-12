package com.securebank.permission;

import com.securebank.common.UserContext;
import com.securebank.repositories.UserPermissionRepository;
import com.securebank.repositories.UserRepository;
import com.securebank.user.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component("bankSecurity")
@RequiredArgsConstructor
@Slf4j
public class BankPermissionEvaluator {

    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;

    // Simple check — no amount
    public boolean can(String permissionCode) {
        return can(permissionCode, null);
    }

    // Amount-aware check
    public boolean can(String permissionCode, BigDecimal amount) {
        try {
            String keycloakId = UserContext.getCurrentKeycloakId();

            // 1. Find user in DB
            UserEntity user = userRepository
                    .findByKeycloakId(keycloakId)
                    .orElse(null);

            if (user == null) {
                log.warn("User not found: {}", keycloakId);
                return false;
            }

            // 2. Find user's permission
            Optional<UserPermissionEntity> userPermission =
                    userPermissionRepository
                            .findByUserIdAndPermissionCode(user.getId(), permissionCode);

            if (userPermission.isEmpty()) {
                log.warn("Permission {} denied for user {}",
                        permissionCode, user.getEmail());
                return false;
            }

            // 3. Check amount limit if provided
            if (amount != null) {
                UserPermissionEntity up = userPermission.get();
                PermissionEntity permission = up.getPermission();

                // Use customLimit if set, otherwise use permission's maxAmount
                BigDecimal limit = up.getCustomLimit() != null
                        ? up.getCustomLimit()
                        : permission.getMaxAmount();

                if (limit != null && amount.compareTo(limit) > 0) {
                    log.warn("Amount {} exceeds limit {} for permission {}",
                            amount, limit, permissionCode);
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            log.error("Permission check failed: {}", e.getMessage());
            return false;
        }
    }
}
