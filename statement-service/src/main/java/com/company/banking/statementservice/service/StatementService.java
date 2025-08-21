package com.company.banking.statementservice.service;

import com.company.banking.grpc.transaction.TransactionHistoryRequest;
import com.company.banking.grpc.transaction.TransactionHistoryResponse;
import com.company.banking.grpc.transaction.TransactionServiceGrpc;
import com.company.banking.statementservice.mapper.TransactionMapper;
import com.company.common.dto.TransactionDTO;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatementService {

    @GrpcClient("transaction-service")
    private TransactionServiceGrpc.TransactionServiceBlockingStub transactionServiceBlockingStub;

    private final TransactionMapper transactionMapper;

    public List<TransactionDTO> getStatement(Long accountId) {
        TransactionHistoryResponse response = transactionServiceBlockingStub.getTransactionsByAccountId(
                TransactionHistoryRequest.newBuilder().setAccountId(accountId).build()
        );
        return response.getTransactionsList().stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }
}
