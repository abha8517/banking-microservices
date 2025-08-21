package com.company.banking.transactionservice.service;

import com.company.banking.grpc.account.AccountRequest;
import com.company.banking.grpc.account.AccountResponse;
import com.company.banking.grpc.account.AccountServiceGrpc;
import com.company.banking.transactionservice.dto.DepositRequest;
import com.company.banking.transactionservice.dto.TransferRequest;
import com.company.banking.transactionservice.dto.WithdrawRequest;
import com.company.banking.transactionservice.exception.InsufficientFundsException;
import com.company.banking.transactionservice.mapper.TransactionMapper;
import com.company.banking.transactionservice.model.Transaction;
import com.company.banking.transactionservice.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountServiceGrpc.AccountServiceBlockingStub accountServiceBlockingStub;
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        org.springframework.test.util.ReflectionTestUtils.setField(transactionService, "accountServiceBlockingStub", accountServiceBlockingStub);
    }

    @Test
    void deposit_shouldSucceed() {
        // Given
        DepositRequest request = new DepositRequest(1L, BigDecimal.TEN, "Test Deposit");
        AccountResponse account = AccountResponse.newBuilder().setId(1L).setBalance("100").build();
        when(accountServiceBlockingStub.getAccountById(any(AccountRequest.class))).thenReturn(account);

        // When
        transactionService.deposit(request);

        // Then
        verify(transactionRepository).save(any(Transaction.class));
        verify(accountServiceBlockingStub).updateAccountBalance(any());
    }

    @Test
    void withdraw_whenSufficientFunds_shouldSucceed() {
        // Given
        WithdrawRequest request = new WithdrawRequest(1L, BigDecimal.TEN, "Test Withdraw");
        AccountResponse account = AccountResponse.newBuilder().setId(1L).setBalance("100").build();
        when(accountServiceBlockingStub.getAccountById(any(AccountRequest.class))).thenReturn(account);

        // When
        transactionService.withdraw(request);

        // Then
        verify(transactionRepository).save(any(Transaction.class));
        verify(accountServiceBlockingStub).updateAccountBalance(any());
    }

    @Test
    void withdraw_whenInsufficientFunds_shouldThrowException() {
        // Given
        WithdrawRequest request = new WithdrawRequest(1L, new BigDecimal("200"), "Test Withdraw");
        AccountResponse account = AccountResponse.newBuilder().setId(1L).setBalance("100").build();
        when(accountServiceBlockingStub.getAccountById(any(AccountRequest.class))).thenReturn(account);

        // When & Then
        assertThatThrownBy(() -> transactionService.withdraw(request))
                .isInstanceOf(InsufficientFundsException.class);
        verify(transactionRepository, never()).save(any());
        verify(accountServiceBlockingStub, never()).updateAccountBalance(any());
    }

    @Test
    void transfer_whenSufficientFunds_shouldSucceed() {
        // Given
        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.TEN, "Test Transfer");
        AccountResponse fromAccount = AccountResponse.newBuilder().setId(1L).setBalance("100").build();
        AccountResponse toAccount = AccountResponse.newBuilder().setId(2L).setBalance("50").build();
        when(accountServiceBlockingStub.getAccountById(AccountRequest.newBuilder().setAccountId(1L).build())).thenReturn(fromAccount);
        when(accountServiceBlockingStub.getAccountById(AccountRequest.newBuilder().setAccountId(2L).build())).thenReturn(toAccount);

        // When
        transactionService.transfer(request);

        // Then
        verify(transactionRepository, times(1)).saveAll(any(List.class));
        verify(accountServiceBlockingStub, times(2)).updateAccountBalance(any());
    }

    @Test
    void transfer_whenInsufficientFunds_shouldThrowException() {
        // Given
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("200"), "Test Transfer");
        AccountResponse fromAccount = AccountResponse.newBuilder().setId(1L).setBalance("100").build();
        AccountResponse toAccount = AccountResponse.newBuilder().setId(2L).setBalance("50").build();
        when(accountServiceBlockingStub.getAccountById(AccountRequest.newBuilder().setAccountId(1L).build())).thenReturn(fromAccount);
        when(accountServiceBlockingStub.getAccountById(AccountRequest.newBuilder().setAccountId(2L).build())).thenReturn(toAccount);

        // When & Then
        assertThatThrownBy(() -> transactionService.transfer(request))
                .isInstanceOf(InsufficientFundsException.class);
        verify(transactionRepository, never()).saveAll(any());
        verify(accountServiceBlockingStub, never()).updateAccountBalance(any());
    }
}
