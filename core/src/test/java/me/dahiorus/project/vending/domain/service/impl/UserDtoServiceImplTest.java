package me.dahiorus.project.vending.domain.service.impl;

import static me.dahiorus.project.vending.domain.service.validation.FieldValidationError.fieldError;
import static me.dahiorus.project.vending.util.ValidationTestUtils.successResults;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
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

import me.dahiorus.project.vending.domain.dao.BinaryDataDao;
import me.dahiorus.project.vending.domain.dao.UserDao;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.AppUser;
import me.dahiorus.project.vending.domain.model.BinaryData;
import me.dahiorus.project.vending.domain.model.dto.BinaryDataDto;
import me.dahiorus.project.vending.domain.model.dto.EditPasswordDto;
import me.dahiorus.project.vending.domain.model.dto.UserDto;
import me.dahiorus.project.vending.domain.service.validation.FieldValidationError;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;
import me.dahiorus.project.vending.domain.service.validation.impl.PasswordValidatorImpl;
import me.dahiorus.project.vending.domain.service.validation.impl.UserDtoValidator;
import me.dahiorus.project.vending.util.UserBuilder;

@ExtendWith(MockitoExtension.class)
class UserDtoServiceImplTest
{
  @Captor
  ArgumentCaptor<AppUser> userArg;

  @Mock
  UserDao dao;

  @Mock
  UserDtoValidator dtoValidator;

  @Mock
  PasswordValidatorImpl passwordValidator;

  @Mock
  BinaryDataDao binaryDataDao;

  PasswordEncoder passwordEncoder;

  UserDtoServiceImpl dtoService;

  @BeforeEach
  void setUp()
  {
    when(dao.getDomainClass()).thenReturn(AppUser.class);
    passwordEncoder = new BCryptPasswordEncoder();
    dtoService = new UserDtoServiceImpl(dao, new DtoMapperImpl(), dtoValidator, passwordValidator, passwordEncoder,
        binaryDataDao);
  }

  @Test
  void createUserWithoutPassword() throws Exception
  {
    UserDto dto = UserBuilder.builder()
      .buildDto();
    when(dtoValidator.validate(dto)).thenReturn(successResults());
    when(dao.save(any())).then(returnsFirstArg());

    UserDto createdUser = dtoService.create(dto);

    assertThat(createdUser.getPassword()).isNull();
  }

  @Nested
  class CreateWithPasswordTests
  {
    @Test
    void createUserWithPassword() throws Exception
    {
      UserDto user = buildUser("User", "Test", "email@yopmail.com", "Secret123");

      when(dtoValidator.validate(user)).thenReturn(successResults());
      when(passwordValidator.validate("password", user.getPassword())).thenReturn(successResults());
      when(dao.save(userArg.capture())).then(invocation -> {
        AppUser toCreate = invocation.getArgument(0);
        toCreate.setId(UUID.randomUUID());
        return toCreate;
      });

      UserDto createdUser = dtoService.create(user);

      assertAll(() -> assertThat(createdUser).hasFieldOrPropertyWithValue("firstName", user.getFirstName())
        .hasFieldOrPropertyWithValue("lastName", user.getLastName())
        .hasFieldOrPropertyWithValue("email", user.getEmail()),
        () -> assertThat(passwordEncoder.matches("Secret123", userArg.getValue()
          .getEncodedPassword())).describedAs("Encoded password must match the raw password")
            .isTrue());
    }

    @Test
    void passwordMustRespectPolicy() throws Exception
    {
      UserDto user = buildUser("User", "Test", "email@yopmail.com", "Secret123");

      when(dtoValidator.validate(user)).thenReturn(successResults());
      when(passwordValidator.validate("password", user.getPassword())).then(invocation -> {
        ValidationResults results = new ValidationResults();
        results.addError(FieldValidationError.fieldError("password", "validation.constraints.password.min-length",
          "Password too short", 12));
        return results;
      });

      assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> dtoService.create(user));
      verify(dao, never()).save(any());
    }

    UserDto buildUser(final String firstName, final String lastName, final String email,
      final String rawPassword)
    {
      return UserBuilder.builder()
        .firstName(firstName)
        .lastName(lastName)
        .email(email)
        .password(rawPassword)
        .buildDto();
    }
  }

  @Nested
  class UpdateTests
  {
    @Test
    void updateUserWithPassword() throws Exception
    {
      UUID id = UUID.randomUUID();
      UserDto dto = UserBuilder.builder()
        .id(id)
        .password("Secret123")
        .buildDto();
      AppUser user = UserBuilder.builder()
        .id(id)
        .encodedPassword(passwordEncoder.encode("Secret"))
        .build();

      when(dao.read(id)).thenReturn(user);
      when(passwordValidator.validate("password", dto.getPassword())).thenReturn(successResults());
      when(dtoValidator.validate(dto)).thenReturn(successResults());
      when(dao.save(user)).thenReturn(user);

      dtoService.update(id, dto);

      assertThat(passwordEncoder.matches("Secret123", user.getEncodedPassword())).isTrue();
    }

    @Test
    void updateUserWithoutPassword() throws Exception
    {
      UUID id = UUID.randomUUID();
      UserDto dto = UserBuilder.builder()
        .id(id)
        .buildDto();
      AppUser user = UserBuilder.builder()
        .id(id)
        .encodedPassword(passwordEncoder.encode("Secret"))
        .build();

      when(dao.read(id)).thenReturn(user);
      when(dtoValidator.validate(dto)).thenReturn(successResults());
      when(dao.save(user)).thenReturn(user);

      dtoService.update(id, dto);

      assertAll(() -> assertThat(passwordEncoder.matches("Secret", user.getEncodedPassword())).isTrue(),
        () -> assertThat(user.getPassword()).isNull());
    }
  }

  @Nested
  class UpdatePasswordTests
  {
    @Test
    void updatePassword() throws Exception
    {
      AppUser user = buildUser(UUID.randomUUID(), "Secret123");

      EditPasswordDto editPwd = new EditPasswordDto("Secret123", "Secret1234");

      when(dao.read(user.getId())).thenReturn(user);
      when(passwordValidator.validate("password", editPwd.password())).thenReturn(successResults());
      when(dao.save(user)).then(invoc -> {
        AppUser arg = invoc.getArgument(0);
        assertThat(passwordEncoder.matches(editPwd.password(), arg.getEncodedPassword())).isTrue();
        return arg;
      });

      assertThatNoException().isThrownBy(() -> dtoService.updatePassword(user.getId(), editPwd));
    }

    @Test
    void oldPasswordMustMatchCurrentOne() throws Exception
    {
      AppUser user = buildUser(UUID.randomUUID(), "Secret123");

      EditPasswordDto editPwd = new EditPasswordDto("Azertyui", "Secret1234");

      when(dao.read(user.getId())).thenReturn(user);
      when(passwordValidator.validate("password", editPwd.password())).thenReturn(successResults());

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

      EditPasswordDto editPwd = new EditPasswordDto("Secret123", "Secret123");

      when(dao.read(user.getId())).thenReturn(user);
      when(passwordValidator.validate("password", editPwd.password())).thenReturn(successResults());

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

      EditPasswordDto editPwd = new EditPasswordDto("Secret123", "Secret1234");

      when(dao.read(user.getId())).thenReturn(user);
      when(passwordValidator.validate("password", editPwd.password())).then(invoc -> {
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
        .isThrownBy(() -> dtoService.updatePassword(id, new EditPasswordDto(null, null)));
      verify(dao, never()).save(any());
    }

    AppUser buildUser(final UUID id, final String password)
    {
      return UserBuilder.builder()
        .id(id)
        .encodedPassword(passwordEncoder.encode(password))
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

      UserDto userDto = dtoService.getByUsername(user.getEmail());

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

  @Nested
  class GetPictureTests
  {
    @Test
    void getPicture() throws Exception
    {
      AppUser user = buildUser(UUID.randomUUID(), "picture.jpg", "image/jpg");
      when(dao.read(user.getId())).thenReturn(user);

      Optional<BinaryDataDto> image = dtoService.getImage(user.getId());

      assertThat(image).isNotEmpty();
    }

    @Test
    void getEmptyPicture() throws Exception
    {
      AppUser user = buildUser(UUID.randomUUID(), null, null);
      user.setPicture(null);
      when(dao.read(user.getId())).thenReturn(user);

      Optional<BinaryDataDto> image = dtoService.getImage(user.getId());

      assertThat(image).isEmpty();
    }

    @Test
    void getFromNonExistingUser() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(dao.read(id)).thenThrow(new EntityNotFound(AppUser.class, id));

      assertThatExceptionOfType(EntityNotFound.class).isThrownBy(() -> dtoService.getImage(id));
    }

    AppUser buildUser(final UUID id, final String pictureName, final String contentType)
    {
      return UserBuilder.builder()
        .id(id)
        .picture(pictureName, contentType)
        .build();
    }
  }

  @Nested
  class UploadImageTests
  {
    @Test
    void uploadImage() throws Exception
    {
      AppUser user = buildUser(UUID.randomUUID());

      when(dao.read(user.getId())).thenReturn(user);
      when(binaryDataDao.save(any())).then(invoc -> {
        BinaryData binary = invoc.getArgument(0);
        binary.setId(UUID.randomUUID());
        return binary;
      });
      when(dao.save(user)).thenReturn(user);

      BinaryDataDto dto = buildBinary("picture.jpg", "image/jpg");
      UserDto updatedUser = dtoService.uploadImage(user.getId(), dto);

      assertThat(updatedUser.getPictureId()).isEqualTo(user.getPicture()
        .getId());
    }

    @Test
    void uploadImageToNonExistingUser() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(dao.read(id)).thenThrow(new EntityNotFound(AppUser.class, id));

      assertThatExceptionOfType(EntityNotFound.class)
        .isThrownBy(() -> dtoService.uploadImage(id, new BinaryDataDto()));
      verify(binaryDataDao, never()).save(any());
      verify(dao, never()).save(any());
    }

    AppUser buildUser(final UUID id)
    {
      return UserBuilder.builder()
        .id(id)
        .build();
    }

    BinaryDataDto buildBinary(final String name, final String contentType)
    {
      BinaryDataDto dto = new BinaryDataDto();
      dto.setName(name);
      dto.setContentType(contentType);
      dto.setContent(new byte[0]);

      return dto;
    }
  }
}
