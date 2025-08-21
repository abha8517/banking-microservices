package com.company.banking.transactionservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record WithdrawRequest(
        @NotNull Long accountId,
        @NotNull @Positive BigDecimal amount,
        String description
) {}
