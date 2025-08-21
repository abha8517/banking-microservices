package com.company.banking.personservice.mapper;

import com.company.banking.personservice.model.Person;
import com.company.common.dto.PersonDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PersonMapper {

    PersonMapper INSTANCE = Mappers.getMapper(PersonMapper.class);

    @Mapping(source = "phoneNumber", target = "phone")
    PersonDTO toDto(Person person);

    @Mapping(source = "phone", target = "phoneNumber")
    Person toEntity(PersonDTO personDTO);
}
