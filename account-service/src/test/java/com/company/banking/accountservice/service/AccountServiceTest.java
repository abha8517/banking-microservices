package com.company.banking.accountservice.service;

import com.company.banking.accountservice.exception.AccountNotFoundException;
import com.company.banking.accountservice.exception.PersonNotFoundException;
import com.company.banking.accountservice.mapper.AccountMapper;
import com.company.banking.accountservice.model.Account;
import com.company.banking.accountservice.repository.AccountRepository;
import com.company.banking.grpc.person.PersonRequest;
import com.company.banking.grpc.person.PersonResponse;
import com.company.banking.grpc.person.PersonServiceGrpc;
import com.company.common.dto.AccountDTO;
import com.company.common.dto.AccountType;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
    private PersonServiceGrpc.PersonServiceBlockingStub personServiceBlockingStub;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        // This is a bit of a hack to make the @InjectMocks work with the @GrpcClient field.
        // A better solution would be constructor injection in the service.
        // But since the service is already written, this is a less intrusive way to test.
        // In a real project, I would refactor the service to use constructor injection.
        org.springframework.test.util.ReflectionTestUtils.setField(accountService, "personServiceBlockingStub", personServiceBlockingStub);
    }

    @Test
    void createAccount_whenPersonExists_shouldCreateAccount() {
        // Given
        Long personId = 1L;
        AccountType accountType = AccountType.SAVINGS;
        PersonResponse personResponse = PersonResponse.newBuilder().setId(personId).build();

        when(personServiceBlockingStub.getPersonById(any(PersonRequest.class))).thenReturn(personResponse);
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
        when(personServiceBlockingStub.getPersonById(any(PersonRequest.class)))
                .thenThrow(new StatusRuntimeException(Status.NOT_FOUND));

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
