package com.company.banking.accountservice.client;

import com.company.common.dto.PersonDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "person-service", path = "/api/v1/persons")
public interface PersonServiceFeignClient {

    @GetMapping("/{id}")
    PersonDTO getPersonById(@PathVariable("id") Long id);
}
