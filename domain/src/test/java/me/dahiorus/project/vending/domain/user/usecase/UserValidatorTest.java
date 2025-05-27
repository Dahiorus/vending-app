package me.dahiorus.project.vending.domain.user.usecase;

import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import me.dahiorus.project.vending.domain.exception.InvalidBusinessObject;
import me.dahiorus.project.vending.domain.user.entity.AppUserToCreate;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.Firstname;
import me.dahiorus.project.vending.domain.user.entity.Lastname;
import me.dahiorus.project.vending.domain.user.entity.Password;
import me.dahiorus.project.vending.domain.user.entity.UserId;
import me.dahiorus.project.vending.domain.user.port.UserToCreateRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

  @Mock UserToCreateRepositoryPort userToCreateRepository;
  @InjectMocks UserValidator userValidator;

  @Test
  void should_be_valid() {
    var userToCreate =
        new AppUserToCreate(
            EmailAddress.of("user@test.org"),
            Password.of("password"),
            Firstname.of("User"),
            Lastname.of("Test"));

    given(userToCreateRepository.findDuplicateOf(userToCreate)).willReturn(empty());

    assertThatCode(() -> userValidator.validate(userToCreate)).doesNotThrowAnyException();
  }

  @Test
  void should_throw_exception_when_duplicate_found() {
    var userToCreate =
        new AppUserToCreate(
            EmailAddress.of("user@test.org"),
            Password.of("password"),
            Firstname.of("User"),
            Lastname.of("Test"));

    given(userToCreateRepository.findDuplicateOf(userToCreate))
        .willReturn(Optional.of(new UserId(randomUUID())));

    assertThatThrownBy(() -> userValidator.validate(userToCreate))
        .isInstanceOf(InvalidBusinessObject.class)
        .hasMessage("1 error(s) found in invalid business object: " + userToCreate);
  }
}
