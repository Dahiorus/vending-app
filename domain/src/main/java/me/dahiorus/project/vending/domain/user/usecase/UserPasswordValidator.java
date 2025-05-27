package me.dahiorus.project.vending.domain.user.usecase;

import static java.lang.String.format;
import static java.util.function.Predicate.not;
import static me.dahiorus.project.vending.domain.validation.ValidationResults.validationResults;

import java.util.function.Predicate;
import me.dahiorus.project.vending.domain.documentation.DomainService;
import me.dahiorus.project.vending.domain.exception.InvalidBusinessObject;
import me.dahiorus.project.vending.domain.user.entity.Password;
import me.dahiorus.project.vending.domain.user.port.PasswordPolicy;
import me.dahiorus.project.vending.domain.validation.FieldValidationError;
import me.dahiorus.project.vending.domain.validation.ValidationResults;
import me.dahiorus.project.vending.domain.validation.Validator;

@DomainService
public class UserPasswordValidator implements Validator<Password> {
  public static final String FIELD_PASSWORD = "password";

  private final PasswordPolicy passwordPolicy;

  public UserPasswordValidator(final PasswordPolicy passwordPolicy) {
    this.passwordPolicy = passwordPolicy;
  }

  @Override
  public ValidationResults buildValidation(final Password rawPassword)
      throws InvalidBusinessObject {
    if (rawPassword.isEmpty()) {
      return validationResults();
    }

    var results = validationResults();

    passwordPolicy
        .maybeMinLength()
        .filter(minLength -> rawPassword.length() < minLength)
        .ifPresent(
            minLength ->
                results.addError(
                    passwordError(
                        "validation.constraints.password.min-length",
                        format("A password must contain at least %d character(s)", minLength),
                        minLength)));

    passwordPolicy
        .maybeMaxLength()
        .filter(maxLength -> rawPassword.length() > maxLength)
        .ifPresent(
            maxLength ->
                results.addError(
                    passwordError(
                        "validation.constraints.password.max-length",
                        format("A password must contain at most %d character(s)", maxLength),
                        maxLength)));

    passwordPolicy
        .maybeMinLowerCaseCharCount()
        .filter(
            minLowerCaseCount ->
                countCharType(rawPassword.value(), Character::isLowerCase) < minLowerCaseCount)
        .ifPresent(
            minLowerCaseCount ->
                results.addError(
                    passwordError(
                        "validation.constraints.password.min-lowercase-chars",
                        format(
                            "A password must contain at least %d lower case character(s)",
                            minLowerCaseCount),
                        minLowerCaseCount)));

    passwordPolicy
        .maybeMinUpperCaseCharCount()
        .filter(
            minUpperCaseCount ->
                countCharType(rawPassword.value(), Character::isUpperCase) < minUpperCaseCount)
        .ifPresent(
            minUpperCaseCount ->
                results.addError(
                    passwordError(
                        "validation.constraints.password.min-uppercase-chars",
                        format(
                            "A password must contain at least %d upper case character(s)",
                            minUpperCaseCount),
                        minUpperCaseCount)));

    passwordPolicy
        .maybeMinDigitCount()
        .filter(
            minDigitCount -> countCharType(rawPassword.value(), Character::isDigit) < minDigitCount)
        .ifPresent(
            minDigitCount ->
                results.addError(
                    passwordError(
                        "validation.constraints.password.min-digits",
                        format("A password must contain at least %d digit(s)", minDigitCount),
                        minDigitCount)));

    passwordPolicy
        .maybeMinSpecialCharCount()
        .filter(
            minSpecialCharsCount ->
                countCharType(rawPassword.value(), not(Character::isLetterOrDigit))
                    < minSpecialCharsCount)
        .ifPresent(
            minSpecialCharsCount ->
                results.addError(
                    passwordError(
                        "validation.constraints.password.min-special-chars",
                        format(
                            "A password must contain at least %d special character(s)",
                            minSpecialCharsCount),
                        minSpecialCharsCount)));

    return results;
  }

  private static FieldValidationError passwordError(
      String code, String defaultMessage, Object... messageArgs) {
    return new FieldValidationError(FIELD_PASSWORD, code, defaultMessage, messageArgs);
  }

  private static long countCharType(
      final String rawPassword, final Predicate<Character> charPredicate) {
    return rawPassword.chars().mapToObj(c -> (char) c).filter(charPredicate).count();
  }
}
