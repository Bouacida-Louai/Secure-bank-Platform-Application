package com.securebank.user;

import com.securebank.common.UserContext;
import com.securebank.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSyncService {

    private final UserRepository userRepository;

    @Transactional
    public UserEntity syncUser() {
        String keycloakId = UserContext.getCurrentKeycloakId();

        // If user already exists → return it
        return userRepository.findByKeycloakId(keycloakId)
                .orElseGet(() -> createUserFromJwt(keycloakId));
    }

    private UserEntity createUserFromJwt(String keycloakId) {
        log.info("Creating new user from JWT: {}", keycloakId);

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
