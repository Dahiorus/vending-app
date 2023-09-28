package me.dahiorus.project.vending.domain.service.validation.impl;

import static me.dahiorus.project.vending.util.CoreTestUtils.anySpec;
import static me.dahiorus.project.vending.util.ValidationTestUtils.assertHasExactlyFieldErrors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.dahiorus.project.vending.domain.dao.UserDao;
import me.dahiorus.project.vending.domain.model.dto.UserDto;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;

@ExtendWith(MockitoExtension.class)
class UserDtoValidatorTest
{
  @Mock
  UserDao dao;

  UserDtoValidator validator;

  UserDto buildDto(final String firstName, final String lastName,
    final String email)
  {
    UserDto dto = new UserDto();
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
    UserDto dto = buildDto("User", "Test", "user.test@yopmail.com");

    ValidationResults results = validator.validate(dto);

    assertThat(results.hasError()).as("No error on %s", dto)
      .isFalse();
  }

  @ParameterizedTest(name = "First name [{0}] is invalid")
  @NullAndEmptySource
  @ValueSource(strings = "   ")
  void firstNameIsMandatory(final String firstName)
  {
    UserDto dto = buildDto(firstName, "Test", "user.test@yopmail.com");

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "firstName",
      "validation.constraints.empty_value");
  }

  @Test
  void firstNameHasMaxLength()
  {
    UserDto dto = buildDto(RandomStringUtils.random(256), "Test",
      "user.test@yopmail.com");

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "firstName",
      "validation.constraints.max_length");
  }

  @ParameterizedTest(name = "Last name [{0}] is invalid")
  @NullAndEmptySource
  @ValueSource(strings = "   ")
  void lastNameIsMandatory(final String lastName)
  {
    UserDto dto = buildDto("User", lastName, "user.test@yopmail.com");

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "lastName",
      "validation.constraints.empty_value");
  }

  @Test
  void lastNameHasMaxLength()
  {
    UserDto dto = buildDto("User", RandomStringUtils.random(256),
      "user.test@yopmail.com");

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "lastName",
      "validation.constraints.max_length");
  }

  @ParameterizedTest(name = "Email [{0}] is invalid")
  @NullAndEmptySource
  @ValueSource(strings = "   ")
  void emailIsMandatory(final String email)
  {
    UserDto dto = buildDto("User", "Test", email);

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "email",
      "validation.constraints.empty_value");
  }

  @Test
  void emailIsUnique()
  {
    UserDto dto = buildDto("User", "test", "user@yopmail.com");
    when(dao.count(anySpec())).thenReturn(1L);

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "email",
      "validation.constraints.not_unique");
  }

  @Test
  void emailHasMaxLength()
  {
    UserDto dto = buildDto("User", "Test", RandomStringUtils.random(256));

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "email",
      "validation.constraints.max_length");
  }
}
