package com.company.banking.personservice.service;

import com.company.banking.personservice.exception.PersonNotFoundException;
import com.company.banking.personservice.mapper.PersonMapper;
import com.company.banking.personservice.model.Person;
import com.company.banking.personservice.repository.PersonRepository;
import com.company.common.dto.PersonDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    @Transactional
    public PersonDTO createPerson(PersonDTO personDTO) {
        personRepository.findByEmail(personDTO.email()).ifPresent(p -> {
            throw new com.company.banking.personservice.exception.PersonAlreadyExistsException("Person with email " + personDTO.email() + " already exists.");
        });
        personRepository.findByPhoneNumber(personDTO.phone()).ifPresent(p -> {
            throw new com.company.banking.personservice.exception.PersonAlreadyExistsException("Person with phone number " + personDTO.phone() + " already exists.");
        });

        Person person = personMapper.toEntity(personDTO);
        Person savedPerson = personRepository.save(person);
        return personMapper.toDto(savedPerson);
    }

    @Transactional(readOnly = true)
    public PersonDTO getPersonById(Long id) {
        return personRepository.findById(id)
                .map(personMapper::toDto)
                .orElseThrow(() -> new PersonNotFoundException("Person not found with id: " + id));
    }

    @Transactional
    public PersonDTO updatePerson(Long id, PersonDTO personDTO) {
        Person existingPerson = personRepository.findById(id)
                .orElseThrow(() -> new PersonNotFoundException("Person not found with id: " + id));

        // Update fields
        existingPerson.setFirstName(personDTO.firstName());
        existingPerson.setLastName(personDTO.lastName());
        existingPerson.setEmail(personDTO.email());
        existingPerson.setPhoneNumber(personDTO.phone());

        Person updatedPerson = personRepository.save(existingPerson);
        return personMapper.toDto(updatedPerson);
    }

    @Transactional
    public void deletePerson(Long id) {
        if (!personRepository.existsById(id)) {
            throw new PersonNotFoundException("Person not found with id: " + id);
        }
        personRepository.deleteById(id);
    }
}
