package com.company.banking.accountservice.controller;

import com.company.banking.accountservice.dto.AccountCreateRequest;
import com.company.banking.accountservice.service.AccountService;
import com.company.common.dto.AccountDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDTO createAccount(@Valid @RequestBody AccountCreateRequest request) {
        return accountService.createAccount(request.personId(), request.accountType());
    }

    @GetMapping("/person/{personId}")
    public List<AccountDTO> getAccountsByPersonId(@PathVariable Long personId) {
        return accountService.getAccountsByPersonId(personId);
    }

    @GetMapping("/{accountId}")
    public AccountDTO getAccountById(@PathVariable Long accountId) {
        return accountService.getAccountById(accountId);
    }
}
