package com.securebank.loan.dtos;

import com.securebank.loan.LoanStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class LoanResponse {
    private Long id;
    private String applicantName;
    private BigDecimal amount;
    private Integer termMonths;
    private BigDecimal interestRate;
    private LoanStatus status;
    private String reviewedBy;
    private String rejectionReason;
    private BigDecimal monthlyInstallment;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}