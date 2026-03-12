package com.securebank.permission;

import com.securebank.common.UserContext;
import com.securebank.repositories.PermissionRepository;
import com.securebank.repositories.UserPermissionRepository;
import com.securebank.repositories.UserRepository;
import com.securebank.user.UserEntity;
import com.securebank.user.UserRole;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSyncService {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final UserPermissionRepository userPermissionRepository;

    @Transactional
    public UserEntity syncUser() {
        String keycloakId = UserContext.getCurrentKeycloakId();
        return userRepository.findByKeycloakId(keycloakId)
                .orElseGet(() -> {
                    UserEntity user = createUserFromJwt(keycloakId);
                    assignDefaultPermissions(user);
                    return user;
                });
    }

    private void assignDefaultPermissions(UserEntity user) {
        List<String> permissionCodes = switch (user.getRole()) {
            case TELLER -> List.of(
                    "account:create",
                    "transaction:deposit",
                    "transaction:withdraw",
                    "transaction:transfer"
            );
            case LOAN_OFFICER -> List.of(
                    "loan:approve",
                    "loan:apply"
            );
            case RISK_ANALYST -> List.of(
                    "account:freeze",
                    "report:export"
            );
            case AUDITOR -> List.of(
                    "report:export"
            );
            case CUSTOMER -> List.of(
                    "loan:apply",
                    "transaction:transfer"
            );
            case SUPER_ADMIN -> permissionRepository
                    .findAll()
                    .stream()
                    .map(PermissionEntity::getCode)
                    .toList();
        };

        permissionCodes.forEach(code -> {
            permissionRepository.findByCode(code).ifPresent(permission -> {
                UserPermissionEntity up = UserPermissionEntity.builder()
                        .user(user)
                        .permission(permission)
                        .customLimit(null)
                        .build();
                userPermissionRepository.save(up);
            });
        });

        log.info("Assigned {} permissions to user {}",
                permissionCodes.size(), user.getEmail());
    }

    private UserEntity createUserFromJwt(String keycloakId) {
        UserEntity user = UserEntity.builder()
                .keycloakId(keycloakId)
                .email(UserContext.getCurrentEmail())
                .fullName(UserContext.getCurrentFullName())
                .branchId(UserContext.getBranchId())
                .role(parseRole(UserContext.getCurrentRole()))
                .build();
        return userRepository.save(user);
    }

    private UserRole parseRole(String role) {
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (Exception e) {
            return UserRole.CUSTOMER;
        }
    }
}