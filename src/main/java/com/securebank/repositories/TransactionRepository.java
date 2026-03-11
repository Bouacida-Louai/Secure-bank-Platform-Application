package com.securebank.repositories;

import com.securebank.transaction.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    List<TransactionEntity> findAllByFromAccountIdOrToAccountId(
            Long fromId, Long toId
    );

    List<TransactionEntity> findAllByInitiatedById(Long userId);

    @Query("""
        SELECT t FROM TransactionEntity t
        WHERE (t.fromAccount.id = :accountId OR t.toAccount.id = :accountId)
        ORDER BY t.createdAt DESC
        """)
    List<TransactionEntity> findTransactionHistory(@Param("accountId") Long accountId);

}

