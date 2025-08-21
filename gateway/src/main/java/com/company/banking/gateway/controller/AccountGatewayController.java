package com.company.banking.gateway.controller;

import com.company.banking.grpc.account.AccountListResponse;
import com.company.banking.grpc.account.AccountRequest;
import com.company.banking.grpc.account.AccountResponse;
import com.company.banking.grpc.account.AccountServiceGrpc;
import com.company.banking.grpc.account.AccountsByPersonIdRequest;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountGatewayController {

    @GrpcClient("account-service")
    private AccountServiceGrpc.AccountServiceBlockingStub accountServiceBlockingStub;

    @GetMapping("/{accountId}")
    public AccountResponse getAccountById(@PathVariable Long accountId) {
        return accountServiceBlockingStub.getAccountById(
                AccountRequest.newBuilder().setAccountId(accountId).build()
        );
    }

    @GetMapping("/person/{personId}")
    public AccountListResponse getAccountsByPersonId(@PathVariable Long personId) {
        return accountServiceBlockingStub.getAccountsByPersonId(
                AccountsByPersonIdRequest.newBuilder().setPersonId(personId).build()
        );
    }
}
