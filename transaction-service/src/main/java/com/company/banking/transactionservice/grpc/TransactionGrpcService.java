package com.company.banking.transactionservice.grpc;

import com.company.banking.grpc.transaction.*;
import com.company.banking.transactionservice.dto.DepositRequest;
import com.company.banking.transactionservice.dto.TransferRequest;
import com.company.banking.transactionservice.dto.WithdrawRequest;
import com.company.banking.transactionservice.service.TransactionService;
import com.company.common.dto.TransactionDTO;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
public class TransactionGrpcService extends TransactionServiceGrpc.TransactionServiceImplBase {

    private final TransactionService transactionService;

    @Override
    public void deposit(com.company.banking.grpc.transaction.DepositRequest request, StreamObserver<Empty> responseObserver) {
        transactionService.deposit(new DepositRequest(request.getAccountId(), new BigDecimal(request.getAmount()), request.getDescription()));
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void withdraw(com.company.banking.grpc.transaction.WithdrawRequest request, StreamObserver<Empty> responseObserver) {
        transactionService.withdraw(new WithdrawRequest(request.getAccountId(), new BigDecimal(request.getAmount()), request.getDescription()));
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void transfer(com.company.banking.grpc.transaction.TransferRequest request, StreamObserver<Empty> responseObserver) {
        transactionService.transfer(new TransferRequest(request.getFromAccountId(), request.getToAccountId(), new BigDecimal(request.getAmount()), request.getDescription()));
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void getTransactionsByAccountId(TransactionHistoryRequest request, StreamObserver<TransactionHistoryResponse> responseObserver) {
        List<TransactionDTO> transactionDTOs = transactionService.getTransactionsByAccountId(request.getAccountId());
        List<TransactionMessage> messages = transactionDTOs.stream()
                .map(this::toTransactionMessage)
                .collect(Collectors.toList());
        TransactionHistoryResponse response = TransactionHistoryResponse.newBuilder().addAllTransactions(messages).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private TransactionMessage toTransactionMessage(TransactionDTO dto) {
        TransactionMessage.Builder builder = TransactionMessage.newBuilder();
        if (dto.id() != null) builder.setId(dto.id());
        if (dto.accountId() != null) builder.setAccountId(dto.accountId());
        if (dto.relatedAccountId() != null) builder.setRelatedAccountId(dto.relatedAccountId());
        if (dto.transactionType() != null) builder.setTransactionType(com.company.banking.grpc.transaction.TransactionType.valueOf(dto.transactionType().name()));
        if (dto.amount() != null) builder.setAmount(dto.amount().toPlainString());
        if (dto.timestamp() != null) builder.setTimestamp(dto.timestamp().toString());
        if (dto.description() != null) builder.setDescription(dto.description());
        return builder.build();
    }
}
