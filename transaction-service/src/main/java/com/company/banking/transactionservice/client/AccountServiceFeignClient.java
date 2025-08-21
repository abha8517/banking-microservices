package com.company.banking.transactionservice.client;

import com.company.common.dto.AccountDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "account-service")
public interface AccountServiceFeignClient {

    @GetMapping("/api/v1/accounts/{accountId}")
    AccountDTO getAccountById(@PathVariable("accountId") Long accountId);

    // This is a simplified internal endpoint for updating balance.
    @PutMapping("/api/v1/internal/accounts/{accountId}/balance")
    void updateAccountBalance(@PathVariable("accountId") Long accountId, @RequestBody BigDecimal newBalance);
}
