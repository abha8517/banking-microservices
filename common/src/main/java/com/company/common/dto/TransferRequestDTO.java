package com.company.common.dto;

import java.math.BigDecimal;

public record TransferRequestDTO(
    Long fromAccountId,
    Long toAccountId,
    BigDecimal amount
) {}
