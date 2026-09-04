package me.dahiorus.project.vending.domain.user.usecase;

import static java.lang.String.format;
import static java.util.function.Predicate.not;
import static me.dahiorus.project.vending.domain.validation.ValidationResults.validationResults;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
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

  private static final List<PasswordRule> RULES =
      List.of(
          new PasswordRule(
              PasswordPolicy::maybeMinLength,
              password -> (long) password.length(),
              (actual, min) -> actual < min,
              "min-length",
              "A password must contain at least %d character(s)"),
          new PasswordRule(
              PasswordPolicy::maybeMaxLength,
              password -> (long) password.length(),
              (actual, max) -> actual > max,
              "max-length",
              "A password must contain at most %d character(s)"),
          new PasswordRule(
              PasswordPolicy::maybeMinLowerCaseCharCount,
              password -> countCharType(password.value(), Character::isLowerCase),
              (actual, min) -> actual < min,
              "min-lowercase-chars",
              "A password must contain at least %d lower case character(s)"),
          new PasswordRule(
              PasswordPolicy::maybeMinUpperCaseCharCount,
              password -> countCharType(password.value(), Character::isUpperCase),
              (actual, min) -> actual < min,
              "min-uppercase-chars",
              "A password must contain at least %d upper case character(s)"),
          new PasswordRule(
              PasswordPolicy::maybeMinDigitCount,
              password -> countCharType(password.value(), Character::isDigit),
              (actual, min) -> actual < min,
              "min-digits",
              "A password must contain at least %d digit(s)"),
          new PasswordRule(
              PasswordPolicy::maybeMinSpecialCharCount,
              password -> countCharType(password.value(), not(Character::isLetterOrDigit)),
              (actual, min) -> actual < min,
              "min-special-chars",
              "A password must contain at least %d special character(s)"));

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

    for (var rule : RULES) {
      rule.threshold()
          .apply(passwordPolicy)
          .filter(threshold -> rule.violates().test(rule.actualValue().apply(rawPassword), threshold))
          .ifPresent(
              threshold ->
                  results.addError(
                      passwordError(
                          "validation.constraints.password." + rule.codeSuffix(),
                          format(rule.messageTemplate(), threshold),
                          threshold)));
    }

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

  private record PasswordRule(
      Function<PasswordPolicy, Optional<Integer>> threshold,
      Function<Password, Long> actualValue,
      BiPredicate<Long, Integer> violates,
      String codeSuffix,
      String messageTemplate) {}
}
