package me.dahiorus.project.vending.domain.user.usecase;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.collection;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mock.Strictness.LENIENT;

import java.util.Optional;
import java.util.Set;
import me.dahiorus.project.vending.domain.exception.InvalidBusinessObject;
import me.dahiorus.project.vending.domain.user.entity.Password;
import me.dahiorus.project.vending.domain.user.port.PasswordPolicy;
import me.dahiorus.project.vending.domain.validation.ValidationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserPasswordValidatorTest {
  @Mock(strictness = LENIENT)
  PasswordPolicy passwordPolicy;

  @InjectMocks UserPasswordValidator validator;

  @BeforeEach
  void setUp() {
    given(passwordPolicy.maybeMinLength()).willReturn(Optional.of(12));
    given(passwordPolicy.maybeMaxLength()).willReturn(Optional.of(24));
    given(passwordPolicy.maybeMinLowerCaseCharCount()).willReturn(Optional.of(1));
    given(passwordPolicy.maybeMinUpperCaseCharCount()).willReturn(Optional.of(1));
    given(passwordPolicy.maybeMinDigitCount()).willReturn(Optional.of(1));
    given(passwordPolicy.maybeMinSpecialCharCount()).willReturn(Optional.of(1));
  }

  @Test
  void should_be_valid_password() {
    assertThatCode(() -> validator.validate(Password.of("Azertyuiop123&")))
        .doesNotThrowAnyException();
  }

  @Test
  void should_have_at_least_min_length() {
    testOn("Secret123&", "validation.constraints.password.min-length");
  }

  @Test
  void should_have_at_least_max_length() {
    testOn(randomAlphabetic(24) + "Az1&", "validation.constraints.password.max-length");
  }

  @Test
  void should_have_at_least_min_lower_case_char() {
    testOn("AZERTYIOP123&", "validation.constraints.password.min-lowercase-chars");
  }

  @Test
  void should_have_at_least_min_upper_case_char() {
    testOn("azertyuiop123&", "validation.constraints.password.min-uppercase-chars");
  }

  @Test
  void should_have_at_least_min_digit() {
    testOn(randomAlphabetic(13) + '&', "validation.constraints.password.min-digits");
  }

  @Test
  void should_have_at_least_min_special_char() {
    testOn(randomAlphanumeric(13) + "Ééù2", "validation.constraints.password.min-special-chars");
  }

  @Test
  void should_return_all_non_respected_password_policies() {
    testOn(
        "secret",
        "validation.constraints.password.min-length",
        "validation.constraints.password.min-uppercase-chars",
        "validation.constraints.password.min-digits",
        "validation.constraints.password.min-special-chars");
  }

  @ParameterizedTest(name = "Empty password [{0}]")
  @NullAndEmptySource
  void should_do_nothing_given_empty_password(final String password) {
    assertThatCode(() -> validator.validate(Password.of(password))).doesNotThrowAnyException();
  }

  @Test
  void should_not_throw_given_no_password_policy() {
    given(passwordPolicy.maybeMinLength()).willReturn(empty());
    given(passwordPolicy.maybeMaxLength()).willReturn(empty());
    given(passwordPolicy.maybeMinLowerCaseCharCount()).willReturn(empty());
    given(passwordPolicy.maybeMinUpperCaseCharCount()).willReturn(empty());
    given(passwordPolicy.maybeMinDigitCount()).willReturn(empty());
    given(passwordPolicy.maybeMinSpecialCharCount()).willReturn(empty());

    assertThatCode(() -> validator.validate(Password.of("Secret"))).doesNotThrowAnyException();
  }

  private void testOn(final String rawPassword, final String... expectedErrorCodes) {
    assertThatThrownBy(() -> validator.validate(Password.of(rawPassword)))
        .isInstanceOf(InvalidBusinessObject.class)
        .asInstanceOf(type(InvalidBusinessObject.class))
        .extracting(this::errorCodes, collection(String.class))
        .containsExactlyInAnyOrder(expectedErrorCodes);
  }

  private Set<String> errorCodes(InvalidBusinessObject ex) {
    return ex.getErrors().stream().map(ValidationError::code).collect(toSet());
  }
}
