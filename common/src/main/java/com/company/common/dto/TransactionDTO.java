package com.company.common.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDTO(
    Long id,
    Long accountId,
    Long relatedAccountId,
    TransactionType transactionType,
    BigDecimal amount,
    LocalDateTime timestamp,
    String description
) {}
