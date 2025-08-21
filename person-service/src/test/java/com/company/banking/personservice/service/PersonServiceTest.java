package com.company.banking.personservice.service;

import com.company.banking.personservice.exception.PersonAlreadyExistsException;
import com.company.banking.personservice.exception.PersonNotFoundException;
import com.company.banking.personservice.mapper.PersonMapper;
import com.company.banking.personservice.model.Person;
import com.company.banking.personservice.repository.PersonRepository;
import com.company.common.dto.PersonDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PersonMapper personMapper;

    @InjectMocks
    private PersonService personService;

    private Person person;
    private PersonDTO personDTO;

    @BeforeEach
    void setUp() {
        person = new Person(1L, "John", "Doe", "john.doe@example.com", "1234567890");
        personDTO = new PersonDTO(1L, "John", "Doe", "john.doe@example.com", "1234567890");
    }

    @Test
    void testCreatePerson_Success() {
        // Given
        PersonDTO request = new PersonDTO(null, "John", "Doe", "john.doe@example.com", "1234567890");
        when(personRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(personRepository.findByPhoneNumber(anyString())).thenReturn(Optional.empty());
        when(personMapper.toEntity(any(PersonDTO.class))).thenReturn(person);
        when(personRepository.save(any(Person.class))).thenReturn(person);
        when(personMapper.toDto(any(Person.class))).thenReturn(personDTO);

        // When
        PersonDTO created = personService.createPerson(request);

        // Then
        assertNotNull(created);
        assertEquals(personDTO.id(), created.id());
        verify(personRepository).save(person);
    }

    @Test
    void testCreatePerson_EmailAlreadyExists_ShouldThrowException() {
        // Given
        PersonDTO request = new PersonDTO(null, "John", "Doe", "john.doe@example.com", "1234567890");
        when(personRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(person));

        // When & Then
        assertThrows(PersonAlreadyExistsException.class, () -> {
            personService.createPerson(request);
        });
        verify(personRepository, never()).save(any());
    }

    @Test
    void testCreatePerson_PhoneNumberAlreadyExists_ShouldThrowException() {
        // Given
        PersonDTO request = new PersonDTO(null, "John", "Doe", "john.doe@example.com", "1234567890");
        when(personRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(personRepository.findByPhoneNumber("1234567890")).thenReturn(Optional.of(person));

        // When & Then
        assertThrows(PersonAlreadyExistsException.class, () -> {
            personService.createPerson(request);
        });
        verify(personRepository, never()).save(any());
    }

    @Test
    void testGetPersonById_Found() {
        when(personRepository.findById(1L)).thenReturn(Optional.of(person));
        when(personMapper.toDto(person)).thenReturn(personDTO);

        PersonDTO found = personService.getPersonById(1L);

        assertNotNull(found);
        assertEquals(personDTO.id(), found.id());
    }

    @Test
    void testGetPersonById_NotFound_ShouldThrowException() {
        when(personRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(PersonNotFoundException.class, () -> {
            personService.getPersonById(2L);
        });
    }

    @Test
    void testUpdatePerson_Success() {
        PersonDTO updateRequest = new PersonDTO(null, "Jane", "Doe", "jane.doe@example.com", "0987654321");
        Person updatedPerson = new Person(1L, "Jane", "Doe", "jane.doe@example.com", "0987654321");
        PersonDTO updatedDTO = new PersonDTO(1L, "Jane", "Doe", "jane.doe@example.com", "0987654321");

        when(personRepository.findById(1L)).thenReturn(Optional.of(person));
        when(personRepository.save(any(Person.class))).thenReturn(updatedPerson);
        when(personMapper.toDto(updatedPerson)).thenReturn(updatedDTO);

        PersonDTO result = personService.updatePerson(1L, updateRequest);

        assertNotNull(result);
        assertEquals("Jane", result.firstName());
        assertEquals("jane.doe@example.com", result.email());
        verify(personRepository).save(person);
    }

    @Test
    void testUpdatePerson_NotFound_ShouldThrowException() {
        PersonDTO updateRequest = new PersonDTO(null, "Jane", "Doe", "jane.doe@example.com", "0987654321");
        when(personRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(PersonNotFoundException.class, () -> {
            personService.updatePerson(2L, updateRequest);
        });
    }

    @Test
    void testDeletePerson_Success() {
        when(personRepository.existsById(1L)).thenReturn(true);
        doNothing().when(personRepository).deleteById(1L);

        assertDoesNotThrow(() -> personService.deletePerson(1L));

        verify(personRepository).deleteById(1L);
    }

    @Test
    void testDeletePerson_NotFound_ShouldThrowException() {
        when(personRepository.existsById(2L)).thenReturn(false);

        assertThrows(PersonNotFoundException.class, () -> {
            personService.deletePerson(2L);
        });
    }
}
