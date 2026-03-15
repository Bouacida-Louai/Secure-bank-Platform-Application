package com.securebank.loan.service;

import com.securebank.audit.service.AuditService;
import com.securebank.loan.LoanEntity;
import com.securebank.loan.LoanStatus;
import com.securebank.loan.RepaymentScheduleEntity;
import com.securebank.loan.dtos.LoanApplicationRequest;
import com.securebank.loan.dtos.LoanDecisionRequest;
import com.securebank.loan.dtos.LoanResponse;
import com.securebank.loan.dtos.RepaymentScheduleResponse;
import com.securebank.permission.UserSyncService;
import com.securebank.repositories.LoanRepository;
import com.securebank.repositories.RepaymentScheduleRepository;
import com.securebank.repositories.UserRepository;
import com.securebank.user.UserEntity;
import com.securebank.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final RepaymentScheduleRepository scheduleRepository;
    private final UserSyncService userSyncService;
    private final AuditService auditService;

    // Fixed interest rate for simplicity
    private static final BigDecimal INTEREST_RATE =
            new BigDecimal("0.12"); // 12% annual
    private static final BigDecimal LARGE_LOAN_THRESHOLD =
            new BigDecimal("50000");

    private LoanResponse toResponse(LoanEntity loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .applicantName(loan.getApplicant().getFullName())
                .amount(loan.getAmount())
                .termMonths(loan.getTermMonths())
                .interestRate(loan.getInterestRate())
                .status(loan.getStatus())
                .reviewedBy(loan.getReviewedBy() != null ?
                        loan.getReviewedBy().getFullName() : null)
                .rejectionReason(loan.getRejectionReason())
                .monthlyInstallment(calculateMonthlyInstallment(
                        loan.getAmount(),
                        loan.getTermMonths(),
                        loan.getInterestRate()
                ))
                .appliedAt(loan.getAppliedAt())
                .updatedAt(loan.getUpdatedAt())
                .build();
    }

    // Monthly installment formula
    private BigDecimal calculateMonthlyInstallment(
            BigDecimal principal,
            Integer termMonths,
            BigDecimal annualRate) {
        // Monthly rate
        BigDecimal monthlyRate = annualRate
                .divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(
                    new BigDecimal(termMonths), 2, RoundingMode.HALF_UP);
        }

        // Formula: P * r * (1+r)^n / ((1+r)^n - 1)
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal power = onePlusR.pow(termMonths);
        BigDecimal numerator = principal
                .multiply(monthlyRate)
                .multiply(power);
        BigDecimal denominator = power.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    // CUSTOMER applies for loan
    @Transactional
    public LoanResponse applyForLoan(LoanApplicationRequest request) {
        UserEntity applicant = userSyncService.syncUser();

        LoanEntity loan = LoanEntity.builder()
                .applicant(applicant)
                .amount(request.getAmount())
                .termMonths(request.getTermMonths())
                .interestRate(INTEREST_RATE)
                .status(LoanStatus.SUBMITTED)
                .build();

        LoanEntity saved = loanRepository.save(loan);
        auditService.log("LOAN_APPLIED", "Loan", saved.getId());
        log.info("Loan applied by {} for ${}",
                applicant.getEmail(), request.getAmount());
        return toResponse(saved);
    }

    // LOAN_OFFICER reviews loan
    @Transactional
    public LoanResponse reviewLoan(Long loanId) {
        LoanEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getStatus() != LoanStatus.SUBMITTED) {
            throw new RuntimeException(
                    "Loan is not in SUBMITTED status");
        }

        loan.setStatus(LoanStatus.UNDER_REVIEW);
        LoanEntity saved = loanRepository.save(loan);
        auditService.log("LOAN_UNDER_REVIEW", "Loan", loanId);
        return toResponse(saved);
    }

    // LOAN_OFFICER approves — amount limit enforced by @PreAuthorize
    @Transactional
    public LoanResponse approveLoan(Long loanId) {
        LoanEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getStatus() != LoanStatus.UNDER_REVIEW
                && loan.getStatus() != LoanStatus.PENDING_MANAGER) {
            throw new RuntimeException(
                    "Loan must be UNDER_REVIEW or PENDING_MANAGER");
        }

        UserEntity reviewer = userSyncService.syncUser();

        // Large loan → escalate to manager
        if (loan.getAmount()
                .compareTo(LARGE_LOAN_THRESHOLD) > 0
                && reviewer.getRole() == UserRole.LOAN_OFFICER) {
            loan.setStatus(LoanStatus.PENDING_MANAGER);
            loan.setReviewedBy(reviewer);
            loanRepository.save(loan);
            auditService.log("LOAN_ESCALATED", "Loan", loanId);
            log.info("Loan {} escalated to manager", loanId);
            return toResponse(loan);
        }

        // Approve directly
        loan.setStatus(LoanStatus.APPROVED);
        loan.setReviewedBy(reviewer);
        loanRepository.save(loan);

        // Generate repayment schedule
        generateRepaymentSchedule(loan);

        auditService.log("LOAN_APPROVED", "Loan", loanId);
        log.info("Loan {} approved by {}", loanId, reviewer.getEmail());
        return toResponse(loan);
    }

    // LOAN_OFFICER or BRANCH_MANAGER rejects
    @Transactional
    public LoanResponse rejectLoan(Long loanId,
                                   LoanDecisionRequest request) {
        LoanEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getStatus() == LoanStatus.APPROVED
                || loan.getStatus() == LoanStatus.REJECTED) {
            throw new RuntimeException(
                    "Cannot reject an already decided loan");
        }

        UserEntity reviewer = userSyncService.syncUser();
        loan.setStatus(LoanStatus.REJECTED);
        loan.setReviewedBy(reviewer);
        loan.setRejectionReason(request.getReason());

        loanRepository.save(loan);
        auditService.log("LOAN_REJECTED", "Loan", loanId);
        return toResponse(loan);
    }

    // Generate monthly repayment schedule
    private void generateRepaymentSchedule(LoanEntity loan) {
        BigDecimal monthlyAmount = calculateMonthlyInstallment(
                loan.getAmount(),
                loan.getTermMonths(),
                loan.getInterestRate()
        );

        LocalDate startDate = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= loan.getTermMonths(); i++) {
            RepaymentScheduleEntity schedule =
                    RepaymentScheduleEntity.builder()
                            .loan(loan)
                            .installmentNumber(i)
                            .dueDate(startDate.plusMonths(i - 1))
                            .amount(monthlyAmount)
                            .paid(false)
                            .build();
            scheduleRepository.save(schedule);
        }

        log.info("Generated {} installments for loan {}",
                loan.getTermMonths(), loan.getId());
    }

    // Get loan by ID
    public LoanResponse getLoanById(Long id) {
        return toResponse(loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found")));
    }

    // Get my loans (CUSTOMER)
    public List<LoanResponse> getMyLoans() {
        UserEntity user = userSyncService.syncUser();
        return loanRepository.findAllByApplicantId(user.getId())
                .stream().map(this::toResponse).toList();
    }

    // Get pending loans (LOAN_OFFICER)
    public List<LoanResponse> getPendingLoans() {
        return loanRepository.findPendingLoans()
                .stream().map(this::toResponse).toList();
    }

    // Get repayment schedule
    public List<RepaymentScheduleResponse> getRepaymentSchedule(
            Long loanId) {
        return scheduleRepository
                .findAllByLoanIdOrderByInstallmentNumber(loanId)
                .stream()
                .map(s -> RepaymentScheduleResponse.builder()
                        .installmentNumber(s.getInstallmentNumber())
                        .dueDate(s.getDueDate())
                        .amount(s.getAmount())
                        .paid(s.getPaid())
                        .build())
                .toList();
    }
}