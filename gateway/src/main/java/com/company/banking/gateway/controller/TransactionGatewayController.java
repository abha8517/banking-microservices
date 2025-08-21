package com.company.banking.gateway.controller;

import com.company.banking.grpc.transaction.*;
import com.google.protobuf.Empty;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

record DepositRequest(Long accountId, BigDecimal amount, String description) {}
record WithdrawRequest(Long accountId, BigDecimal amount, String description) {}
record TransferRequest(Long fromAccountId, Long toAccountId, BigDecimal amount, String description) {}

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionGatewayController {

    @GrpcClient("transaction-service")
    private TransactionServiceGrpc.TransactionServiceBlockingStub transactionServiceBlockingStub;

    @PostMapping("/deposit")
    public void deposit(@RequestBody DepositRequest request) {
        com.company.banking.grpc.transaction.DepositRequest grpcRequest = com.company.banking.grpc.transaction.DepositRequest.newBuilder()
                .setAccountId(request.accountId())
                .setAmount(request.amount().toPlainString())
                .setDescription(request.description())
                .build();
        transactionServiceBlockingStub.deposit(grpcRequest);
    }

    @PostMapping("/withdraw")
    public void withdraw(@RequestBody WithdrawRequest request) {
        com.company.banking.grpc.transaction.WithdrawRequest grpcRequest = com.company.banking.grpc.transaction.WithdrawRequest.newBuilder()
                .setAccountId(request.accountId())
                .setAmount(request.amount().toPlainString())
                .setDescription(request.description())
                .build();
        transactionServiceBlockingStub.withdraw(grpcRequest);
    }

    @PostMapping("/transfer")
    public void transfer(@RequestBody TransferRequest request) {
        com.company.banking.grpc.transaction.TransferRequest grpcRequest = com.company.banking.grpc.transaction.TransferRequest.newBuilder()
                .setFromAccountId(request.fromAccountId())
                .setToAccountId(request.toAccountId())
                .setAmount(request.amount().toPlainString())
                .setDescription(request.description())
                .build();
        transactionServiceBlockingStub.transfer(grpcRequest);
    }

    @GetMapping("/history/{accountId}")
    public TransactionHistoryResponse getTransactionHistory(@PathVariable Long accountId) {
        return transactionServiceBlockingStub.getTransactionsByAccountId(
                TransactionHistoryRequest.newBuilder().setAccountId(accountId).build()
        );
    }
}
