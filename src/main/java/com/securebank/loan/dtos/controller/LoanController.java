package com.securebank.loan.dtos.controller;

import com.securebank.loan.dtos.LoanApplicationRequest;
import com.securebank.loan.dtos.LoanDecisionRequest;
import com.securebank.loan.dtos.LoanResponse;
import com.securebank.loan.dtos.RepaymentScheduleResponse;
import com.securebank.loan.service.LoanService;
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
public class LoanController {

    private final LoanService loanService;

    // CUSTOMER applies
    @PostMapping("/apply")
    @PreAuthorize("@bankSecurity.can('loan:apply')")
    public ResponseEntity<LoanResponse> apply(
            @Valid @RequestBody LoanApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(loanService.applyForLoan(request));
    }

    // LOAN_OFFICER starts review
    @PatchMapping("/{id}/review")
    @PreAuthorize("hasRole('LOAN_OFFICER')")
    public ResponseEntity<LoanResponse> review(
            @PathVariable Long id) {
        return ResponseEntity.ok(loanService.reviewLoan(id));
    }

    // LOAN_OFFICER approves (under 50K)
    // SUPER_ADMIN approves (any amount)
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER','SUPER_ADMIN')")
    public ResponseEntity<LoanResponse> approve(
            @PathVariable Long id) {
        return ResponseEntity.ok(loanService.approveLoan(id));
    }

    // LOAN_OFFICER or SUPER_ADMIN rejects
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER','SUPER_ADMIN')")
    public ResponseEntity<LoanResponse> reject(
            @PathVariable Long id,
            @RequestBody LoanDecisionRequest request) {
        return ResponseEntity.ok(loanService.rejectLoan(id, request));
    }

    // CUSTOMER sees their loans
    @GetMapping("/my")
    public ResponseEntity<List<LoanResponse>> myLoans() {
        return ResponseEntity.ok(loanService.getMyLoans());
    }

    // LOAN_OFFICER sees pending loans
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER','SUPER_ADMIN')")
    public ResponseEntity<List<LoanResponse>> pendingLoans() {
        return ResponseEntity.ok(loanService.getPendingLoans());
    }

    // Get loan by ID
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponse> getLoan(
            @PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    // Get repayment schedule
    @GetMapping("/{id}/schedule")
    public ResponseEntity<List<RepaymentScheduleResponse>> schedule(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                loanService.getRepaymentSchedule(id));
    }
}