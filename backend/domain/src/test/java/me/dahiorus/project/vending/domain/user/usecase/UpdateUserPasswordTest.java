package me.dahiorus.project.vending.domain.user.usecase;

import static me.dahiorus.project.vending.fixture.UserFixture.aUser;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

import java.util.Set;
import me.dahiorus.project.vending.domain.exception.InvalidBusinessObject;
import me.dahiorus.project.vending.domain.exception.OldPasswordNotMatch;
import me.dahiorus.project.vending.domain.user.entity.EditPassword;
import me.dahiorus.project.vending.domain.user.entity.Password;
import me.dahiorus.project.vending.domain.user.port.AppUserRepositoryPort;
import me.dahiorus.project.vending.domain.user.port.PasswordMatcherPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateUserPasswordTest {

  @Mock AppUserRepositoryPort appUserRepository;
  @Mock PasswordMatcherPort passwordMatcher;
  @Mock UserPasswordValidator userPasswordValidator;
  @InjectMocks UpdateUserPassword updateUserPassword;

  @Test
  void should_update_user_password_with_valid_password() {
    var user = aUser().buildUser();
    var oldPassword = Password.of("secret");
    var newPassword = Password.of("secret123");

    given(passwordMatcher.matches(user.id(), oldPassword)).willReturn(true);

    updateUserPassword.execute(user.id(), new EditPassword(oldPassword, newPassword));

    then(appUserRepository).should().updatePassword(user.id(), newPassword);
    then(passwordMatcher).should().matches(user.id(), oldPassword);
    then(userPasswordValidator).should().validate(newPassword);
  }

  @Test
  void should_throw_exception_when_old_password_not_match() {
    var user = aUser().buildUser();
    var oldPassword = Password.of("secret");
    var newPassword = Password.of("secret123");

    given(passwordMatcher.matches(user.id(), oldPassword)).willReturn(false);

    assertThatThrownBy(
            () -> updateUserPassword.execute(user.id(), new EditPassword(oldPassword, newPassword)))
        .isInstanceOf(OldPasswordNotMatch.class);
    then(appUserRepository).should(never()).updatePassword(user.id(), newPassword);
  }

  @Test
  void should_throw_exception_when_invalid_new_password() {
    var user = aUser().buildUser();
    var oldPassword = Password.of("secret");
    var newPassword = Password.of("secret123");

    given(passwordMatcher.matches(user.id(), oldPassword)).willReturn(true);
    willThrow(new InvalidBusinessObject(newPassword, Set.of()))
        .given(userPasswordValidator)
        .validate(newPassword);

    assertThatThrownBy(
            () -> updateUserPassword.execute(user.id(), new EditPassword(oldPassword, newPassword)))
        .isInstanceOf(InvalidBusinessObject.class);
    then(appUserRepository).should(never()).updatePassword(user.id(), newPassword);
  }
}
