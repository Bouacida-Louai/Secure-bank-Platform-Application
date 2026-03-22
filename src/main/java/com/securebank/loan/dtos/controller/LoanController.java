package com.securebank.loan.dtos.controller;

import com.securebank.loan.dtos.LoanApplicationRequest;
import com.securebank.loan.dtos.LoanDecisionRequest;
import com.securebank.loan.dtos.LoanResponse;
import com.securebank.loan.dtos.RepaymentScheduleResponse;
import com.securebank.loan.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Tag(name = "Loans", description = "Loan lifecycle — apply, review, approve, reject")
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/apply")
    @PreAuthorize("@bankSecurity.can('loan:apply')")
    @Operation(
            summary = "Apply for loan",
            description = "CUSTOMER only — submit a new loan application"
    )
    public ResponseEntity<LoanResponse> apply(
            @Valid @RequestBody LoanApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(loanService.applyForLoan(request));
    }

    @PatchMapping("/{id}/review")
    @PreAuthorize("hasRole('LOAN_OFFICER')")
    @Operation(
            summary = "Start loan review",
            description = "LOAN_OFFICER only — moves loan from SUBMITTED to UNDER_REVIEW"
    )
    public ResponseEntity<LoanResponse> review(
            @PathVariable Long id) {
        return ResponseEntity.ok(loanService.reviewLoan(id));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER','SUPER_ADMIN')")
    @Operation(
            summary = "Approve loan",
            description = "LOAN_OFFICER approves under $50K, loans over $50K escalate to PENDING_MANAGER"
    )
    public ResponseEntity<LoanResponse> approve(
            @PathVariable Long id) {
        return ResponseEntity.ok(loanService.approveLoan(id));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER','SUPER_ADMIN')")
    @Operation(
            summary = "Reject loan",
            description = "LOAN_OFFICER or SUPER_ADMIN — reject with a reason"
    )
    public ResponseEntity<LoanResponse> reject(
            @PathVariable Long id,
            @RequestBody LoanDecisionRequest request) {
        return ResponseEntity.ok(loanService.rejectLoan(id, request));
    }

    @GetMapping("/my")
    @Operation(
            summary = "Get my loans",
            description = "CUSTOMER sees their own loan applications and statuses"
    )
    public ResponseEntity<List<LoanResponse>> myLoans() {
        return ResponseEntity.ok(loanService.getMyLoans());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER','SUPER_ADMIN')")
    @Operation(
            summary = "Get pending loans",
            description = "LOAN_OFFICER and SUPER_ADMIN — view all loans awaiting decision"
    )
    public ResponseEntity<List<LoanResponse>> pendingLoans() {
        return ResponseEntity.ok(loanService.getPendingLoans());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get loan by ID",
            description = "Returns loan details including status and reviewer"
    )
    public ResponseEntity<LoanResponse> getLoan(
            @PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    @GetMapping("/{id}/schedule")
    @Operation(
            summary = "Get repayment schedule",
            description = "Returns monthly installments for an approved loan"
    )
    public ResponseEntity<List<RepaymentScheduleResponse>> schedule(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                loanService.getRepaymentSchedule(id));
    }
}