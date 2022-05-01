package me.dahiorus.project.vending.core.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import me.dahiorus.project.vending.core.dao.UserDAO;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.User;
import me.dahiorus.project.vending.core.model.dto.UserDTO;
import me.dahiorus.project.vending.core.model.dto.UserWithPasswordDTO;
import me.dahiorus.project.vending.core.service.validation.FieldValidationError;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;
import me.dahiorus.project.vending.core.service.validation.impl.PasswordValidatorImpl;
import me.dahiorus.project.vending.core.service.validation.impl.UserDtoValidator;

@ExtendWith(MockitoExtension.class)
class UserDtoServiceImplTest
{
  @Captor
  ArgumentCaptor<User> userArg;

  @Mock
  UserDAO dao;

  @Mock
  UserDtoValidator dtoValidator;

  @Mock
  PasswordValidatorImpl passwordValidator;

  PasswordEncoder passwordEncoder;

  UserDtoServiceImpl dtoService;

  @BeforeEach
  void setUp()
  {
    when(dao.getDomainClass()).thenReturn(User.class);
    passwordEncoder = new BCryptPasswordEncoder();
    dtoService = new UserDtoServiceImpl(dao, new DtoMapperImpl(), dtoValidator, passwordValidator, passwordEncoder);
  }

  @Test
  void createUserWithPassword() throws Exception
  {
    UserWithPasswordDTO user = buildUser("User", "Test", "email@yopmail.com", "Secret123");

    when(dtoValidator.validate(user)).thenReturn(new ValidationResults());
    when(passwordValidator.validate("password", user.getPassword(), true)).thenReturn(new ValidationResults());
    when(dao.save(userArg.capture())).then(invocation -> {
      User toCreate = invocation.getArgument(0);
      toCreate.setId(UUID.randomUUID());
      return toCreate;
    });

    UserDTO createdUser = dtoService.create(user);

    assertAll(() -> assertThat(createdUser).hasFieldOrPropertyWithValue("firstName", user.getFirstName())
      .hasFieldOrPropertyWithValue("lastName", user.getLastName())
      .hasFieldOrPropertyWithValue("email", user.getEmail()),
        () -> assertThat(passwordEncoder.matches("Secret123", userArg.getValue()
          .getPassword())).describedAs("Encoded password must match the raw password")
            .isTrue());
  }

  @Test
  void passwordMustRespectPolicy() throws Exception
  {
    UserWithPasswordDTO user = buildUser("User", "Test", "email@yopmail.com", "Secret123");

    when(dtoValidator.validate(user)).thenReturn(new ValidationResults());
    when(passwordValidator.validate("password", user.getPassword(), true)).then(invocation -> {
      ValidationResults results = new ValidationResults();
      results.addError(FieldValidationError.fieldError("password", "validation.constraints.password.min-length",
          "Password too short", 12));
      return results;
    });

    assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> dtoService.create(user));
    verify(dao, never()).save(any());
  }

  UserWithPasswordDTO buildUser(final String firstName, final String lastName, final String email,
      final String rawPassword)
  {
    UserWithPasswordDTO user = new UserWithPasswordDTO();
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmail(email);
    user.setPassword(rawPassword);

    return user;
  }
}
