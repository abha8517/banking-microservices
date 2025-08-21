package com.company.banking.transactionservice.service;

import com.company.banking.grpc.account.AccountRequest;
import com.company.banking.grpc.account.AccountResponse;
import com.company.banking.grpc.account.AccountServiceGrpc;
import com.company.banking.grpc.account.UpdateBalanceRequest;
import com.company.banking.transactionservice.dto.DepositRequest;
import com.company.banking.transactionservice.dto.TransferRequest;
import com.company.banking.transactionservice.dto.WithdrawRequest;
import com.company.banking.transactionservice.exception.InsufficientFundsException;
import com.company.banking.transactionservice.mapper.TransactionMapper;
import com.company.banking.transactionservice.model.Transaction;
import com.company.banking.transactionservice.repository.TransactionRepository;
import com.company.common.dto.TransactionDTO;
import com.company.common.dto.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @GrpcClient("account-service")
    private AccountServiceGrpc.AccountServiceBlockingStub accountServiceBlockingStub;

    @Transactional
    public void deposit(DepositRequest request) {
        log.info("Processing deposit for accountId: {}", request.accountId());
        // 1. Get account details from account-service via gRPC
        AccountResponse account = getAccount(request.accountId());

        // 2. Create and save transaction log
        Transaction transaction = Transaction.builder()
                .accountId(request.accountId())
                .transactionType(TransactionType.DEPOSIT)
                .amount(request.amount())
                .timestamp(LocalDateTime.now())
                .description(request.description())
                .build();
        transactionRepository.save(transaction);

        // 3. Update account balance via gRPC
        BigDecimal newBalance = new BigDecimal(account.getBalance()).add(request.amount());
        updateAccountBalance(request.accountId(), newBalance);
        log.info("Deposit successful for accountId: {}. New balance: {}", request.accountId(), newBalance);
    }

    @Transactional
    public void withdraw(WithdrawRequest request) {
        log.info("Processing withdrawal for accountId: {}", request.accountId());
        // 1. Get account details
        AccountResponse account = getAccount(request.accountId());
        BigDecimal currentBalance = new BigDecimal(account.getBalance());

        // 2. Check for sufficient funds
        if (currentBalance.compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal.");
        }

        // 3. Create and save transaction log
        Transaction transaction = Transaction.builder()
                .accountId(request.accountId())
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(request.amount().negate())
                .timestamp(LocalDateTime.now())
                .description(request.description())
                .build();
        transactionRepository.save(transaction);

        // 4. Update account balance
        BigDecimal newBalance = currentBalance.subtract(request.amount());
        updateAccountBalance(request.accountId(), newBalance);
        log.info("Withdrawal successful for accountId: {}. New balance: {}", request.accountId(), newBalance);
    }

    @Transactional
    public void transfer(TransferRequest request) {
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new IllegalArgumentException("From and To accounts cannot be the same.");
        }
        log.info("Processing transfer from accountId: {} to accountId: {}", request.fromAccountId(), request.toAccountId());
        // 1. Get details for both accounts
        AccountResponse fromAccount = getAccount(request.fromAccountId());
        AccountResponse toAccount = getAccount(request.toAccountId());
        BigDecimal fromBalance = new BigDecimal(fromAccount.getBalance());
        BigDecimal toBalance = new BigDecimal(toAccount.getBalance());

        // 2. Check for sufficient funds
        if (fromBalance.compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transfer.");
        }

        // 3. Create and save transaction logs for both accounts
        LocalDateTime timestamp = LocalDateTime.now();
        String toDescription = "Transfer to account " + toAccount.getAccountNumber();
        Transaction fromTransaction = Transaction.builder()
                .accountId(request.fromAccountId())
                .relatedAccountId(request.toAccountId())
                .transactionType(TransactionType.TRANSFER)
                .amount(request.amount().negate())
                .timestamp(timestamp)
                .description(toDescription)
                .build();

        String fromDescription = "Transfer from account " + fromAccount.getAccountNumber();
        Transaction toTransaction = Transaction.builder()
                .accountId(request.toAccountId())
                .relatedAccountId(request.fromAccountId())
                .transactionType(TransactionType.TRANSFER)
                .amount(request.amount())
                .timestamp(timestamp)
                .description(fromDescription)
                .build();

        transactionRepository.saveAll(List.of(fromTransaction, toTransaction));

        // 4. Update balances on both accounts
        BigDecimal newFromBalance = fromBalance.subtract(request.amount());
        BigDecimal newToBalance = toBalance.add(request.amount());
        updateAccountBalance(request.fromAccountId(), newFromBalance);
        updateAccountBalance(request.toAccountId(), newToBalance);
        log.info("Transfer successful.");
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByAccountId(Long accountId) {
        log.info("Fetching transactions for accountId: {}", accountId);
        return transactionRepository.findByAccountId(accountId).stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }

    private AccountResponse getAccount(Long accountId) {
        return accountServiceBlockingStub.getAccountById(AccountRequest.newBuilder().setAccountId(accountId).build());
    }

    private void updateAccountBalance(Long accountId, BigDecimal newBalance) {
        accountServiceBlockingStub.updateAccountBalance(UpdateBalanceRequest.newBuilder()
                .setAccountId(accountId)
                .setNewBalance(newBalance.toPlainString())
                .build());
    }
}
