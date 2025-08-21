package com.company.common.dto;

import java.math.BigDecimal;

public record CreateAccountRequestDTO(
    Long personId,
    String accountType,
    BigDecimal initialDeposit
) {}
