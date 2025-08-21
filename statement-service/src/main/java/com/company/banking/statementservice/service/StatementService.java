package com.company.banking.statementservice.service;

import com.company.banking.statementservice.client.TransactionServiceFeignClient;
import com.company.common.dto.TransactionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatementService {

    private final TransactionServiceFeignClient transactionServiceFeignClient;

    public List<TransactionDTO> getStatement(Long accountId) {
        return transactionServiceFeignClient.getTransactionHistory(accountId);
    }
}
