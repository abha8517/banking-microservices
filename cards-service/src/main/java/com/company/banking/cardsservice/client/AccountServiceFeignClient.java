package com.company.banking.cardsservice.client;

import com.company.common.dto.AccountDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "account-service", path = "/api/v1/accounts")
public interface AccountServiceFeignClient {

    @GetMapping("/{accountId}")
    AccountDTO getAccountById(@PathVariable("accountId") Long accountId);
}
