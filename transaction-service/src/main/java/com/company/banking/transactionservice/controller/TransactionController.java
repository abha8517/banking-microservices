package com.company.banking.transactionservice.controller;

import com.company.banking.transactionservice.dto.DepositRequest;
import com.company.banking.transactionservice.dto.TransferRequest;
import com.company.banking.transactionservice.dto.WithdrawRequest;
import com.company.banking.transactionservice.service.TransactionService;
import com.company.common.dto.TransactionDTO;
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
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    @ResponseStatus(HttpStatus.OK)
    public void deposit(@Valid @RequestBody DepositRequest request) {
        transactionService.deposit(request);
    }

    @PostMapping("/withdraw")
    @ResponseStatus(HttpStatus.OK)
    public void withdraw(@Valid @RequestBody WithdrawRequest request) {
        transactionService.withdraw(request);
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.OK)
    public void transfer(@Valid @RequestBody TransferRequest request) {
        transactionService.transfer(request);
    }

    @GetMapping("/history/{accountId}")
    public List<TransactionDTO> getTransactionHistory(@PathVariable Long accountId) {
        return transactionService.getTransactionsByAccountId(accountId);
    }
}
