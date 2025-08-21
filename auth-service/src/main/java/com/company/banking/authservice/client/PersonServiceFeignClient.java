package com.company.banking.authservice.client;

import com.company.common.dto.PersonDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "person-service", path = "/api/v1/persons")
public interface PersonServiceFeignClient {

    @PostMapping
    PersonDTO createPerson(@RequestBody PersonDTO personDTO);
}
