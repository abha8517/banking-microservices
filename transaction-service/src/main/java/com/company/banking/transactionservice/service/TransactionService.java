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
import com.company.common.dto.TransactionDTO;
import com.company.common.dto.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final AccountServiceFeignClient accountServiceFeignClient;
    private final TransactionMapper transactionMapper;

    @Transactional
    public void deposit(DepositRequest request) {
        log.info("Processing deposit for accountId: {}", request.accountId());
        // 1. Get account details
        AccountDTO account = accountServiceFeignClient.getAccountById(request.accountId());

        // 2. Create and save transaction log
        Transaction transaction = Transaction.builder()
                .accountId(request.accountId())
                .transactionType(TransactionType.DEPOSIT)
                .amount(request.amount())
                .timestamp(LocalDateTime.now())
                .description(request.description())
                .build();
        transactionRepository.save(transaction);

        // 3. Update account balance
        BigDecimal newBalance = account.balance().add(request.amount());
        accountServiceFeignClient.updateAccountBalance(request.accountId(), newBalance);
        log.info("Deposit successful for accountId: {}. New balance: {}", request.accountId(), newBalance);
    }

    @Transactional
    public void withdraw(WithdrawRequest request) {
        log.info("Processing withdrawal for accountId: {}", request.accountId());
        // 1. Get account details
        AccountDTO account = accountServiceFeignClient.getAccountById(request.accountId());

        // 2. Check for sufficient funds
        if (account.balance().compareTo(request.amount()) < 0) {
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
        BigDecimal newBalance = account.balance().subtract(request.amount());
        accountServiceFeignClient.updateAccountBalance(request.accountId(), newBalance);
        log.info("Withdrawal successful for accountId: {}. New balance: {}", request.accountId(), newBalance);
    }

    @Transactional
    public void transfer(TransferRequest request) {
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new IllegalArgumentException("From and To accounts cannot be the same.");
        }
        log.info("Processing transfer from accountId: {} to accountId: {}", request.fromAccountId(), request.toAccountId());
        // 1. Get details for both accounts
        AccountDTO fromAccount = accountServiceFeignClient.getAccountById(request.fromAccountId());
        AccountDTO toAccount = accountServiceFeignClient.getAccountById(request.toAccountId());

        // 2. Check for sufficient funds
        if (fromAccount.balance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transfer.");
        }

        // 3. Create and save transaction logs for both accounts
        LocalDateTime timestamp = LocalDateTime.now();
        String toDescription = "Transfer to account " + toAccount.accountNumber();
        Transaction fromTransaction = Transaction.builder()
                .accountId(request.fromAccountId())
                .relatedAccountId(request.toAccountId())
                .transactionType(TransactionType.TRANSFER)
                .amount(request.amount().negate())
                .timestamp(timestamp)
                .description(toDescription)
                .build();

        String fromDescription = "Transfer from account " + fromAccount.accountNumber();
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
        BigDecimal newFromBalance = fromAccount.balance().subtract(request.amount());
        BigDecimal newToBalance = toAccount.balance().add(request.amount());
        accountServiceFeignClient.updateAccountBalance(request.fromAccountId(), newFromBalance);
        accountServiceFeignClient.updateAccountBalance(request.toAccountId(), newToBalance);
        log.info("Transfer successful.");
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByAccountId(Long accountId) {
        log.info("Fetching transactions for accountId: {}", accountId);
        return transactionRepository.findByAccountId(accountId).stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }
}
