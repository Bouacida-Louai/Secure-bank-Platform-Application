package com.securebank.repositories;

import com.securebank.account.AccountEntity;
import com.securebank.account.AccountStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity,Long> {

   List<AccountEntity> findByOwnerId(Long ownweId);
   List<AccountEntity> findAllByOwnerId(Long ownweId);
    Optional<AccountEntity> findByAccountNumber(String accountNumber);
    List<AccountEntity> findAllByOwnerIdAndStatus(Long ownerId, AccountStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountEntity a WHERE a.id = :id")
    Optional<AccountEntity> findByIdWithLock(@Param("id") Long id);
}
