package com.company.banking.accountservice.service;

import com.company.banking.accountservice.client.PersonServiceFeignClient;
import com.company.banking.accountservice.exception.AccountNotFoundException;
import com.company.banking.accountservice.exception.PersonNotFoundException;
import com.company.banking.accountservice.mapper.AccountMapper;
import com.company.banking.accountservice.model.Account;
import com.company.banking.accountservice.repository.AccountRepository;
import com.company.common.dto.AccountDTO;
import com.company.common.dto.AccountType;
import com.company.common.dto.PersonDTO;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private PersonServiceFeignClient personServiceFeignClient;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_whenPersonExists_shouldCreateAccount() {
        // Given
        Long personId = 1L;
        AccountType accountType = AccountType.SAVINGS;
        PersonDTO personDTO = new PersonDTO(personId, "John", "Doe", "john.doe@test.com", "123");

        when(personServiceFeignClient.getPersonById(personId)).thenReturn(personDTO);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId(1L);
            return account;
        });
        when(accountMapper.toDto(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            return new AccountDTO(account.getId(), account.getAccountNumber(), account.getAccountType(), account.getBalance(), account.getPersonId());
        });

        // When
        AccountDTO result = accountService.createAccount(personId, accountType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.personId()).isEqualTo(personId);
        assertThat(result.accountType()).isEqualTo(accountType);
        assertThat(result.balance()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_whenPersonDoesNotExist_shouldThrowException() {
        // Given
        Long personId = 2L;
        Request request = Request.create(Request.HttpMethod.GET, "/api/v1/persons/2", Collections.emptyMap(), null, new RequestTemplate());
        when(personServiceFeignClient.getPersonById(personId)).thenThrow(new FeignException.NotFound("Not Found", request, null, null));

        // When & Then
        assertThatThrownBy(() -> accountService.createAccount(personId, AccountType.CHECKING))
                .isInstanceOf(PersonNotFoundException.class)
                .hasMessage("Person with id " + personId + " not found.");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void getAccountsByPersonId_shouldReturnAccountList() {
        // Given
        Long personId = 1L;
        Account account = Account.builder().id(1L).personId(personId).build();
        AccountDTO accountDTO = new AccountDTO(1L, "123", AccountType.SAVINGS, BigDecimal.TEN, personId);

        when(accountRepository.findByPersonId(personId)).thenReturn(List.of(account));
        when(accountMapper.toDto(account)).thenReturn(accountDTO);

        // When
        List<AccountDTO> result = accountService.getAccountsByPersonId(personId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(accountDTO);
    }

    @Test
    void getAccountById_whenAccountExists_shouldReturnAccount() {
        // Given
        Long accountId = 1L;
        Account account = Account.builder().id(accountId).build();
        AccountDTO accountDTO = new AccountDTO(accountId, "123", AccountType.CHECKING, BigDecimal.ZERO, 1L);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountMapper.toDto(account)).thenReturn(accountDTO);

        // When
        AccountDTO result = accountService.getAccountById(accountId);

        // Then
        assertThat(result).isEqualTo(accountDTO);
    }

    @Test
    void getAccountById_whenAccountDoesNotExist_shouldThrowException() {
        // Given
        Long accountId = 2L;
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> accountService.getAccountById(accountId))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Account not found with id: " + accountId);
    }
}
