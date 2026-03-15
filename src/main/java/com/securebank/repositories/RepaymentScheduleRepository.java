package com.securebank.repositories;

import com.securebank.loan.RepaymentScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepaymentScheduleRepository
        extends JpaRepository<RepaymentScheduleEntity, Long> {

    List<RepaymentScheduleEntity> findAllByLoanIdOrderByInstallmentNumber(
            Long loanId);
}
