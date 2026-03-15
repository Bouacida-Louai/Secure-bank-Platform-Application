package com.securebank.loan.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LoanApplicationRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "100.00", message = "Minimum loan is $100")
    private BigDecimal amount;

    @NotNull(message = "Term is required")
    @Min(value = 3, message = "Minimum term is 3 months")
    @Max(value = 60, message = "Maximum term is 60 months")
    private Integer termMonths;

    private String purpose;
}