package com.company.banking.gateway.controller;

import com.company.banking.grpc.transaction.TransactionHistoryRequest;
import com.company.banking.grpc.transaction.TransactionHistoryResponse;
import com.company.banking.grpc.transaction.TransactionServiceGrpc;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statements")
@RequiredArgsConstructor
public class StatementGatewayController {

    // Note: The statement service calls the transaction service.
    // In a pure gRPC world, the gateway could call the statement service,
    // which then calls the transaction service.
    // For simplicity here, the gateway will call the transaction service directly
    // to get the history, similar to how the statement-service was implemented.
    @GrpcClient("transaction-service")
    private TransactionServiceGrpc.TransactionServiceBlockingStub transactionServiceBlockingStub;

    @GetMapping("/{accountId}")
    public TransactionHistoryResponse getStatement(@PathVariable Long accountId) {
        return transactionServiceBlockingStub.getTransactionsByAccountId(
                TransactionHistoryRequest.newBuilder().setAccountId(accountId).build()
        );
    }
}
