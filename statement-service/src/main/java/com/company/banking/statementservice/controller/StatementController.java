package com.company.banking.statementservice.controller;

import com.company.banking.statementservice.service.StatementService;
import com.company.common.dto.TransactionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/statements")
@RequiredArgsConstructor
public class StatementController {

    private final StatementService statementService;

    @GetMapping("/{accountId}")
    public List<TransactionDTO> getStatement(@PathVariable Long accountId) {
        return statementService.getStatement(accountId);
    }
}
