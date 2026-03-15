package com.securebank.loan.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class RepaymentScheduleResponse {
    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal amount;
    private Boolean paid;
}