package com.company.banking.transactionservice.mapper;

import com.company.banking.transactionservice.model.Transaction;
import com.company.common.dto.TransactionDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionDTO toDto(Transaction transaction);
}
