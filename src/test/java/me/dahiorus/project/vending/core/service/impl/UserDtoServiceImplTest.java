package me.dahiorus.project.vending.core.service.impl;

import static com.dahiorus.project.vending.util.TestUtils.successResults;
import static me.dahiorus.project.vending.core.service.validation.FieldValidationError.fieldError;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dahiorus.project.vending.util.UserBuilder;

import me.dahiorus.project.vending.core.dao.impl.UserDaoImpl;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.AppUser;
import me.dahiorus.project.vending.core.model.dto.EditPasswordDTO;
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
  ArgumentCaptor<AppUser> userArg;

  @Mock
  UserDaoImpl dao;

  @Mock
  UserDtoValidator dtoValidator;

  @Mock
  PasswordValidatorImpl passwordValidator;

  PasswordEncoder passwordEncoder;

  UserDtoServiceImpl dtoService;

  @BeforeEach
  void setUp()
  {
    when(dao.getDomainClass()).thenReturn(AppUser.class);
    passwordEncoder = new BCryptPasswordEncoder();
    dtoService = new UserDtoServiceImpl(dao, new DtoMapperImpl(), dtoValidator, passwordValidator, passwordEncoder);
  }

  @Nested
  class CreateWithPasswordTests
  {
    @Test
    void createUserWithPassword() throws Exception
    {
      UserWithPasswordDTO user = buildUser("User", "Test", "email@yopmail.com", "Secret123");

      when(dtoValidator.validate(user)).thenReturn(successResults());
      when(passwordValidator.validate("password", user.getPassword(), true)).thenReturn(successResults());
      when(dao.save(userArg.capture())).then(invocation -> {
        AppUser toCreate = invocation.getArgument(0);
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

      when(dtoValidator.validate(user)).thenReturn(successResults());
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
      return UserBuilder.builder()
        .firstName(firstName)
        .lastName(lastName)
        .email(email)
        .password(rawPassword)
        .buildDto(UserWithPasswordDTO.class);
    }
  }

  @Nested
  class UpdatePasswordTests
  {
    @Test
    void updatePassword() throws Exception
    {
      AppUser user = buildUser(UUID.randomUUID(), "Secret123");

      EditPasswordDTO editPwd = new EditPasswordDTO();
      editPwd.setOldPassword("Secret123");
      editPwd.setPassword("Secret1234");

      when(dao.read(user.getId())).thenReturn(user);
      when(passwordValidator.validate("password", editPwd.getPassword(), true)).thenReturn(successResults());
      when(dao.save(user)).then(invoc -> {
        AppUser arg = invoc.getArgument(0);
        assertThat(passwordEncoder.matches(editPwd.getPassword(), arg.getPassword())).isTrue();
        return arg;
      });

      assertThatNoException().isThrownBy(() -> dtoService.updatePassword(user.getId(), editPwd));
    }

    @Test
    void oldPasswordMustMatchCurrentOne() throws Exception
    {
      AppUser user = buildUser(UUID.randomUUID(), "Secret123");

      EditPasswordDTO editPwd = new EditPasswordDTO();
      editPwd.setOldPassword("Azertyui");
      editPwd.setPassword("Secret1234");

      when(dao.read(user.getId())).thenReturn(user);
      when(passwordValidator.validate("password", editPwd.getPassword(), true)).thenReturn(successResults());

      assertThatExceptionOfType(ValidationException.class)
        .isThrownBy(() -> dtoService.updatePassword(user.getId(), editPwd))
        .extracting(ValidationException::getFieldErrors)
        .asList()
        .hasSize(1)
        .allSatisfy(err -> assertThat(err).hasFieldOrPropertyWithValue("field", "oldPassword")
          .hasFieldOrPropertyWithValue("code", "validation.constraints.password.old-password-mismatch"));
      verify(dao, never()).save(user);
    }

    @Test
    void newPasswordMustNotMatchCurrentOne() throws Exception
    {
      AppUser user = buildUser(UUID.randomUUID(), "Secret123");

      EditPasswordDTO editPwd = new EditPasswordDTO();
      editPwd.setOldPassword("Secret123");
      editPwd.setPassword("Secret123");

      when(dao.read(user.getId())).thenReturn(user);
      when(passwordValidator.validate("password", editPwd.getPassword(), true)).thenReturn(successResults());

      assertThatExceptionOfType(ValidationException.class)
        .isThrownBy(() -> dtoService.updatePassword(user.getId(), editPwd))
        .extracting(ValidationException::getFieldErrors)
        .asList()
        .hasSize(1)
        .allSatisfy(err -> assertThat(err).hasFieldOrPropertyWithValue("field", "password")
          .hasFieldOrPropertyWithValue("code", "validation.constraints.password.new-password-match"));
      verify(dao, never()).save(user);
    }

    @Test
    void newPasswordMustBeValid() throws Exception
    {
      AppUser user = buildUser(UUID.randomUUID(), "Secret123");

      EditPasswordDTO editPwd = new EditPasswordDTO();
      editPwd.setOldPassword("Secret123");
      editPwd.setPassword("Secret1234");

      when(dao.read(user.getId())).thenReturn(user);
      when(passwordValidator.validate("password", editPwd.getPassword(), true)).then(invoc -> {
        ValidationResults results = new ValidationResults();
        results.addError(
            fieldError(invoc.getArgument(0), "validation.constraints.password.min-length", "Pwd error from test"));
        return results;
      });

      assertThatExceptionOfType(ValidationException.class)
        .isThrownBy(() -> dtoService.updatePassword(user.getId(), editPwd));
      verify(dao, never()).save(user);
    }

    @Test
    void userMustExist() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(dao.read(id)).thenThrow(new EntityNotFound(AppUser.class, id));

      assertThatExceptionOfType(EntityNotFound.class)
        .isThrownBy(() -> dtoService.updatePassword(id, new EditPasswordDTO()));
      verify(dao, never()).save(any());
    }

    AppUser buildUser(final UUID id, final String password)
    {
      return UserBuilder.builder()
        .id(id)
        .password(passwordEncoder.encode(password))
        .build();
    }
  }

  @Nested
  class GetByUsernameTests
  {
    @Test
    void getByUsername() throws Exception
    {
      AppUser user = buildUser("user@test.fr");
      when(dao.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

      UserDTO userDto = dtoService.getByUsername(user.getEmail());

      assertThat(userDto.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void getByUsernameNotFound()
    {
      String username = "azerty";
      when(dao.findByEmail(username)).thenReturn(Optional.empty());

      assertThatExceptionOfType(EntityNotFound.class).isThrownBy(() -> dtoService.getByUsername(username));
    }

    AppUser buildUser(final String username)
    {
      return UserBuilder.builder()
        .email(username)
        .build();
    }
  }
}
