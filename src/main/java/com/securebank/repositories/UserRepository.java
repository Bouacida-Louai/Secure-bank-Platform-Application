package com.securebank.repositories;

import com.securebank.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {

    Optional<UserEntity> findByKeycloakId(String keycloakId);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByKeycloakId(String keycloakId);
}
