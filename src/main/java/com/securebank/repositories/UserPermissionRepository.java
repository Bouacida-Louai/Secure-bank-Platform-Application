package com.securebank.repositories;

import com.securebank.permission.UserPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPermissionRepository
        extends JpaRepository<UserPermissionEntity, Long> {

    List<UserPermissionEntity> findAllByUserId(Long userId);

    @Query("""
        SELECT up FROM UserPermissionEntity up
        WHERE up.user.id = :userId
        AND up.permission.code = :code
        """)
    Optional<UserPermissionEntity> findByUserIdAndPermissionCode(
            @Param("userId") Long userId,
            @Param("code") String code
    );
}