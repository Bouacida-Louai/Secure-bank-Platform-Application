package com.securebank.dtos;

import com.securebank.account.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class CreateAccountRequest {

    @NotNull(message = "Account type is required")
    private AccountType type;

    @NotNull(message = "Owner ID is required")
    private Long ownerId;

    private BigDecimal initialDeposit = BigDecimal.ZERO;

}
