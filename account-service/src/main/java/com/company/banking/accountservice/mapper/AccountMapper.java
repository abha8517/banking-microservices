package com.company.banking.accountservice.mapper;

import com.company.banking.accountservice.model.Account;
import com.company.common.dto.AccountDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountDTO toDto(Account account);

    Account toEntity(AccountDTO accountDTO);
}
