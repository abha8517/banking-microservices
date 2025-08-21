package com.company.banking.transactionservice.service;

import com.company.banking.transactionservice.client.AccountServiceFeignClient;
import com.company.banking.transactionservice.dto.DepositRequest;
import com.company.banking.transactionservice.dto.TransferRequest;
import com.company.banking.transactionservice.dto.WithdrawRequest;
import com.company.banking.transactionservice.exception.InsufficientFundsException;
import com.company.banking.transactionservice.mapper.TransactionMapper;
import com.company.banking.transactionservice.model.Transaction;
import com.company.banking.transactionservice.repository.TransactionRepository;
import com.company.common.dto.AccountDTO;
import com.company.common.dto.AccountType;
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
    private AccountServiceFeignClient accountServiceFeignClient;
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void deposit_shouldSucceed() {
        // Given
        DepositRequest request = new DepositRequest(1L, BigDecimal.TEN, "Test Deposit");
        AccountDTO account = new AccountDTO(1L, "123", AccountType.SAVINGS, new BigDecimal("100"), 1L);
        when(accountServiceFeignClient.getAccountById(1L)).thenReturn(account);

        // When
        transactionService.deposit(request);

        // Then
        verify(transactionRepository).save(any(Transaction.class));
        verify(accountServiceFeignClient).updateAccountBalance(1L, new BigDecimal("110"));
    }

    @Test
    void withdraw_whenSufficientFunds_shouldSucceed() {
        // Given
        WithdrawRequest request = new WithdrawRequest(1L, BigDecimal.TEN, "Test Withdraw");
        AccountDTO account = new AccountDTO(1L, "123", AccountType.SAVINGS, new BigDecimal("100"), 1L);
        when(accountServiceFeignClient.getAccountById(1L)).thenReturn(account);

        // When
        transactionService.withdraw(request);

        // Then
        verify(transactionRepository).save(any(Transaction.class));
        verify(accountServiceFeignClient).updateAccountBalance(1L, new BigDecimal("90"));
    }

    @Test
    void withdraw_whenInsufficientFunds_shouldThrowException() {
        // Given
        WithdrawRequest request = new WithdrawRequest(1L, new BigDecimal("200"), "Test Withdraw");
        AccountDTO account = new AccountDTO(1L, "123", AccountType.SAVINGS, new BigDecimal("100"), 1L);
        when(accountServiceFeignClient.getAccountById(1L)).thenReturn(account);

        // When & Then
        assertThatThrownBy(() -> transactionService.withdraw(request))
                .isInstanceOf(InsufficientFundsException.class);
        verify(transactionRepository, never()).save(any());
        verify(accountServiceFeignClient, never()).updateAccountBalance(any(), any());
    }

    @Test
    void transfer_whenSufficientFunds_shouldSucceed() {
        // Given
        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.TEN, "Test Transfer");
        AccountDTO fromAccount = new AccountDTO(1L, "123", AccountType.SAVINGS, new BigDecimal("100"), 1L);
        AccountDTO toAccount = new AccountDTO(2L, "456", AccountType.CHECKING, new BigDecimal("50"), 2L);
        when(accountServiceFeignClient.getAccountById(1L)).thenReturn(fromAccount);
        when(accountServiceFeignClient.getAccountById(2L)).thenReturn(toAccount);

        // When
        transactionService.transfer(request);

        // Then
        verify(transactionRepository, times(1)).saveAll(any(List.class));
        verify(accountServiceFeignClient, times(1)).updateAccountBalance(1L, new BigDecimal("90"));
        verify(accountServiceFeignClient, times(1)).updateAccountBalance(2L, new BigDecimal("60"));
    }

    @Test
    void transfer_whenInsufficientFunds_shouldThrowException() {
        // Given
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("200"), "Test Transfer");
        AccountDTO fromAccount = new AccountDTO(1L, "123", AccountType.SAVINGS, new BigDecimal("100"), 1L);
        AccountDTO toAccount = new AccountDTO(2L, "456", AccountType.CHECKING, new BigDecimal("50"), 2L);
        when(accountServiceFeignClient.getAccountById(1L)).thenReturn(fromAccount);
        when(accountServiceFeignClient.getAccountById(2L)).thenReturn(toAccount);

        // When & Then
        assertThatThrownBy(() -> transactionService.transfer(request))
                .isInstanceOf(InsufficientFundsException.class);
        verify(transactionRepository, never()).saveAll(any());
        verify(accountServiceFeignClient, never()).updateAccountBalance(any(), any());
    }
}
