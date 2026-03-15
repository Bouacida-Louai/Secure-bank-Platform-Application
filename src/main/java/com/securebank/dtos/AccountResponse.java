package com.securebank.dtos;

import com.securebank.account.AccountStatus;
import com.securebank.account.AccountType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@Builder
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private AccountType type;
    private AccountStatus status;
    private String ownerName;
    private String ownerEmail;
    private LocalDateTime createdAt;

}
