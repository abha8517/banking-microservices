package com.company.banking.statementservice.mapper;

import com.company.banking.grpc.transaction.TransactionMessage;
import com.company.common.dto.TransactionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ValueMapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @ValueMapping(source = "TRANSACTION_TYPE_UNSPECIFIED", target = "<NULL>")
    @ValueMapping(source = "UNRECOGNIZED", target = "<NULL>")
    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.parse(message.getTimestamp()))")
    @Mapping(target = "amount", expression = "java(new java.math.BigDecimal(message.getAmount()))")
    TransactionDTO toDto(TransactionMessage message);
}
