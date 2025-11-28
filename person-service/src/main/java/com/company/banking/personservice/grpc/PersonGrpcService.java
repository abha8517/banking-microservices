package com.company.banking.personservice.grpc;

import com.company.banking.grpc.person.CreatePersonRequest;
import com.company.banking.grpc.person.DeletePersonRequest;
import com.company.banking.grpc.person.DeletePersonResponse;
import com.company.banking.grpc.person.PersonRequest;
import com.company.banking.grpc.person.PersonResponse;
import com.company.banking.grpc.person.PersonServiceGrpc;
import com.company.banking.grpc.person.UpdatePersonRequest;
import com.company.banking.personservice.exception.PersonAlreadyExistsException;
import com.company.banking.personservice.exception.PersonNotFoundException;
import com.company.banking.personservice.service.PersonService;
import com.company.common.dto.PersonDTO;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class PersonGrpcService extends PersonServiceGrpc.PersonServiceImplBase {

    private final PersonService personService;

    @Override
    public void getPersonById(PersonRequest request, StreamObserver<PersonResponse> responseObserver) {
        try {
            PersonDTO personDTO = personService.getPersonById(request.getPersonId());
            PersonResponse response = PersonResponse.newBuilder()
                    .setId(personDTO.id())
                    .setFirstName(personDTO.firstName())
                    .setLastName(personDTO.lastName())
                    .setEmail(personDTO.email())
                    .setPhone(personDTO.phone())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (PersonNotFoundException e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void createPerson(CreatePersonRequest request, StreamObserver<PersonResponse> responseObserver) {
        try {
            PersonDTO personToCreate = new PersonDTO(null, request.getFirstName(), request.getLastName(), request.getEmail(), request.getPhone());
            PersonDTO createdPerson = personService.createPerson(personToCreate);
            PersonResponse response = PersonResponse.newBuilder()
                    .setId(createdPerson.id())
                    .setFirstName(createdPerson.firstName())
                    .setLastName(createdPerson.lastName())
                    .setEmail(createdPerson.email())
                    .setPhone(createdPerson.phone())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (PersonAlreadyExistsException e) {
            responseObserver.onError(io.grpc.Status.ALREADY_EXISTS
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void updatePerson(UpdatePersonRequest request, StreamObserver<PersonResponse> responseObserver) {
        try {
            PersonDTO personToUpdate = new PersonDTO(request.getId(), request.getFirstName(), request.getLastName(), request.getEmail(), request.getPhone());
            PersonDTO updatedPerson = personService.updatePerson(request.getId(), personToUpdate);
            PersonResponse response = PersonResponse.newBuilder()
                    .setId(updatedPerson.id())
                    .setFirstName(updatedPerson.firstName())
                    .setLastName(updatedPerson.lastName())
                    .setEmail(updatedPerson.email())
                    .setPhone(updatedPerson.phone())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (PersonNotFoundException e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void deletePerson(DeletePersonRequest request, StreamObserver<DeletePersonResponse> responseObserver) {
        try {
            personService.deletePerson(request.getId());
            responseObserver.onNext(DeletePersonResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (PersonNotFoundException e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}
