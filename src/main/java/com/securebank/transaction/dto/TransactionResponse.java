package com.securebank.transaction.dto;

import com.securebank.transaction.TransactionStatus;
import com.securebank.transaction.TransactionType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TransactionResponse {
    private Long id;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private String initiatedBy;
    private String description;
    private LocalDateTime createdAt;
}