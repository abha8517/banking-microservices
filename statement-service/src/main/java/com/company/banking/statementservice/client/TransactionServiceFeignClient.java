package com.company.banking.statementservice.client;

import com.company.common.dto.TransactionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "transaction-service", path = "/api/v1/transactions")
public interface TransactionServiceFeignClient {

    @GetMapping("/history/{accountId}")
    List<TransactionDTO> getTransactionHistory(@PathVariable("accountId") Long accountId);
}
