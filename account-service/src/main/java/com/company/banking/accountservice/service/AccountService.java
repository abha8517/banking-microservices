package com.company.banking.accountservice.service;

import com.company.banking.accountservice.client.PersonServiceFeignClient;
import com.company.banking.accountservice.exception.AccountNotFoundException;
import com.company.banking.accountservice.exception.PersonNotFoundException;
import com.company.banking.accountservice.mapper.AccountMapper;
import com.company.banking.accountservice.model.Account;
import com.company.banking.accountservice.repository.AccountRepository;
import com.company.common.dto.AccountDTO;
import com.company.common.dto.AccountType;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final PersonServiceFeignClient personServiceFeignClient;

    @Transactional
    public AccountDTO createAccount(Long personId, AccountType accountType) {
        log.info("Request to create a {} account for personId: {}", accountType, personId);

        // Step 1: Validate that the person exists by calling person-service
        try {
            personServiceFeignClient.getPersonById(personId);
            log.info("Successfully validated person with id: {}", personId);
        } catch (FeignException.NotFound e) {
            log.error("Person with id {} not found via person-service.", personId);
            throw new PersonNotFoundException("Person with id " + personId + " not found.");
        }

        // Step 2: Create the new account
        Account account = Account.builder()
                .personId(personId)
                .accountType(accountType)
                .balance(BigDecimal.ZERO)
                // In a real system, this should be a more robust, sequential number generator
                .accountNumber(UUID.randomUUID().toString().replace("-", ""))
                .build();

        Account savedAccount = accountRepository.save(account);
        log.info("Successfully created account with id {} and number {}", savedAccount.getId(), savedAccount.getAccountNumber());
        return accountMapper.toDto(savedAccount);
    }

    @Transactional(readOnly = true)
    public List<AccountDTO> getAccountsByPersonId(Long personId) {
        log.info("Fetching all accounts for personId: {}", personId);
        return accountRepository.findByPersonId(personId).stream()
                .map(accountMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountDTO getAccountById(Long accountId) {
        log.info("Fetching account with id: {}", accountId);
        return accountRepository.findById(accountId)
                .map(accountMapper::toDto)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountId));
    }

    @Transactional
    public void updateBalance(Long accountId, BigDecimal newBalance) {
        log.info("Updating balance for accountId: {} to new balance: {}", accountId, newBalance);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountId));

        account.setBalance(newBalance);
        accountRepository.save(account);
        log.info("Successfully updated balance for accountId: {}", accountId);
    }
}
