package com.company.banking.accountservice.dto;

import com.company.common.dto.AccountType;
import jakarta.validation.constraints.NotNull;

public record AccountCreateRequest(
        @NotNull Long personId,
        @NotNull AccountType accountType
) {}
