package com.company.banking.gateway.controller;

import com.company.banking.grpc.person.PersonRequest;
import com.company.banking.grpc.person.PersonResponse;
import com.company.banking.grpc.person.PersonServiceGrpc;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/persons")
@RequiredArgsConstructor
public class PersonGatewayController {

    @GrpcClient("person-service")
    private PersonServiceGrpc.PersonServiceBlockingStub personServiceBlockingStub;

    // This is just an example. I will need to create a DTO and a mapper.
    // For now, I will just return the gRPC response directly to test the concept.
    @GetMapping("/{personId}")
    public PersonResponse getPersonById(@PathVariable Long personId) {
        return personServiceBlockingStub.getPersonById(
                PersonRequest.newBuilder().setPersonId(personId).build()
        );
    }
}
