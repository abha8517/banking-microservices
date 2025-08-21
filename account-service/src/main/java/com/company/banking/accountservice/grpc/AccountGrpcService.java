package com.company.banking.accountservice.grpc;

import com.company.banking.accountservice.service.AccountService;
import com.company.banking.grpc.account.AccountListResponse;
import com.company.banking.grpc.account.AccountRequest;
import com.company.banking.grpc.account.AccountResponse;
import com.company.banking.grpc.account.AccountServiceGrpc;
import com.company.banking.grpc.account.AccountsByPersonIdRequest;
import com.company.banking.grpc.account.UpdateBalanceRequest;
import com.company.common.dto.AccountDTO;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
public class AccountGrpcService extends AccountServiceGrpc.AccountServiceImplBase {

    private final AccountService accountService;

    @Override
    public void getAccountById(AccountRequest request, StreamObserver<AccountResponse> responseObserver) {
        try {
            AccountDTO accountDTO = accountService.getAccountById(request.getAccountId());
            AccountResponse response = toAccountResponse(accountDTO);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getAccountsByPersonId(AccountsByPersonIdRequest request, StreamObserver<AccountListResponse> responseObserver) {
        List<AccountDTO> accountDTOs = accountService.getAccountsByPersonId(request.getPersonId());
        List<AccountResponse> responses = accountDTOs.stream().map(this::toAccountResponse).collect(Collectors.toList());
        AccountListResponse response = AccountListResponse.newBuilder().addAllAccounts(responses).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateAccountBalance(UpdateBalanceRequest request, StreamObserver<Empty> responseObserver) {
        accountService.updateBalance(request.getAccountId(), new BigDecimal(request.getNewBalance()));
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private AccountResponse toAccountResponse(AccountDTO accountDTO) {
        return AccountResponse.newBuilder()
                .setId(accountDTO.id())
                .setAccountNumber(accountDTO.accountNumber())
                .setAccountType(com.company.banking.grpc.account.AccountType.valueOf(accountDTO.accountType().name()))
                .setBalance(accountDTO.balance().toPlainString())
                .setPersonId(accountDTO.personId())
                .build();
    }
}
