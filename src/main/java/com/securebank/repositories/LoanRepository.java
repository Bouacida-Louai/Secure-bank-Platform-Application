package com.securebank.repositories;

import com.securebank.loan.LoanEntity;
import com.securebank.loan.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<LoanEntity, Long> {

    List<LoanEntity> findAllByApplicantId(Long applicantId);

    List<LoanEntity> findAllByStatus(LoanStatus status);

    @Query("""
        SELECT l FROM LoanEntity l
        WHERE l.status IN ('SUBMITTED', 'UNDER_REVIEW', 'PENDING_MANAGER')
        ORDER BY l.appliedAt ASC
        """)
    List<LoanEntity> findPendingLoans();
}