package com.company.common.dto;

import java.math.BigDecimal;

public record AccountDTO(
    Long id,
    String accountNumber,
    AccountType accountType,
    BigDecimal balance,
    Long personId
) {}
