package com.securebank.loan.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoanDecisionRequest {

    private String reason; // required for rejection
}