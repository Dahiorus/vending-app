package me.dahiorus.project.vending.core.service.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.dahiorus.project.vending.core.dao.impl.UserDaoImpl;
import me.dahiorus.project.vending.core.model.User;
import me.dahiorus.project.vending.core.model.dto.UserDTO;
import me.dahiorus.project.vending.core.service.validation.ValidationError;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;

@ExtendWith(MockitoExtension.class)
class UserDtoValidatorTest
{
  @Mock
  UserDaoImpl dao;

  UserDtoValidator validator;

  UserDTO buildDto(final String firstName, final String lastName, final String email)
  {
    UserDTO dto = new UserDTO();
    dto.setEmail(email);
    dto.setFirstName(firstName);
    dto.setLastName(lastName);

    return dto;
  }

  @BeforeEach
  void setUp()
  {
    validator = new UserDtoValidator(dao);
  }

  @Test
  void dtoIsValid()
  {
    UserDTO dto = buildDto("User", "Test", "user.test@yopmail.com");

    ValidationResults results = validator.validate(dto);

    assertThat(results.hasError()).as("No error on %s", dto)
      .isFalse();
  }

  @ParameterizedTest(name = "First name [{0}] is invalid")
  @NullAndEmptySource
  @ValueSource(strings = "   ")
  void firstNameIsMandatory(final String firstName)
  {
    UserDTO dto = buildDto(firstName, "Test", "user.test@yopmail.com");

    ValidationResults results = validator.validate(dto);

    assertThat(results.getFieldErrors("firstName")).map(ValidationError::getCode)
      .containsOnly("validation.constraints.empty_value");
  }

  @ParameterizedTest(name = "Last name [{0}] is invalid")
  @NullAndEmptySource
  @ValueSource(strings = "   ")
  void lastNameIsMandatory(final String lastName)
  {
    UserDTO dto = buildDto("User", lastName, "user.test@yopmail.com");

    ValidationResults results = validator.validate(dto);

    assertThat(results.getFieldErrors("lastName")).map(ValidationError::getCode)
      .containsOnly("validation.constraints.empty_value");
  }

  @ParameterizedTest(name = "Email [{0}] is invalid")
  @NullAndEmptySource
  @ValueSource(strings = "   ")
  void emailIsMandatory(final String email)
  {
    UserDTO dto = buildDto("User", "Test", email);

    ValidationResults results = validator.validate(dto);

    assertThat(results.getFieldErrors("email")).map(ValidationError::getCode)
      .containsOnly("validation.constraints.empty_value");
  }

  @Test
  void emailIsUnique()
  {
    UserDTO dto = buildDto("User", "test", "user@yopmail.com");
    User duplicate = new User();
    duplicate.setId(UUID.randomUUID());
    duplicate.setEmail(dto.getEmail());

    when(dao.findByEmail(dto.getEmail())).thenReturn(Optional.of(duplicate));

    ValidationResults results = validator.validate(dto);

    assertThat(results.getFieldErrors("email")).map(ValidationError::getCode)
      .containsOnly("validation.constraints.not_unique");
  }
}
