package com.company.banking.accountservice.controller;

import com.company.banking.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/internal/accounts")
@RequiredArgsConstructor
public class InternalAccountController {

    private final AccountService accountService;

    @PutMapping("/{accountId}/balance")
    @ResponseStatus(HttpStatus.OK)
    public void updateAccountBalance(@PathVariable Long accountId, @RequestBody BigDecimal newBalance) {
        accountService.updateBalance(accountId, newBalance);
    }
}
