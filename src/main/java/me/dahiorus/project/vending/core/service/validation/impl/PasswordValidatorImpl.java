package me.dahiorus.project.vending.core.service.validation.impl;

import static java.util.function.Predicate.not;
import static me.dahiorus.project.vending.core.service.validation.FieldValidationError.emptyOrNullValue;
import static me.dahiorus.project.vending.core.service.validation.FieldValidationError.fieldError;
import static me.dahiorus.project.vending.core.service.validation.ValidationError.getFullCode;

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.config.PasswordPolicyProperties;
import me.dahiorus.project.vending.core.service.validation.PasswordValidator;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;

@Component
@Log4j2
@AllArgsConstructor
public class PasswordValidatorImpl implements PasswordValidator
{
  private static final String DEFAULT_MSG_PREFIX = "A password must contain at least ";

  private final PasswordPolicyProperties passwordPolicyProps;

  @Override
  public ValidationResults validate(final String field, final String rawPassword, final boolean mandatory)
  {
    log.debug("Validating the password respects the current password policy");

    ValidationResults results = new ValidationResults();

    if (StringUtils.isEmpty(rawPassword))
    {
      if (!mandatory)
      {
        log.debug("Empty password, but not mandatory. Nothing to validate.");
        return results;
      }
      results.addError(emptyOrNullValue(field));
    }

    Integer minLength = passwordPolicyProps.getMinLength();
    if (minLength != null && StringUtils.length(rawPassword) < minLength)
    {
      results.addError(fieldError(field, getFullCode("password.min-length"),
          DEFAULT_MSG_PREFIX + minLength + " character(s)", minLength));
    }

    Integer maxLength = passwordPolicyProps.getMaxLength();
    if (maxLength != null && StringUtils.length(rawPassword) > maxLength)
    {
      results.addError(fieldError(field, getFullCode("password.max-length"),
          "A password must contain at most " + maxLength + " character(s)", maxLength));
    }

    Integer minLowerCaseCount = passwordPolicyProps.getMinLowerCaseCount();
    if (minLowerCaseCount != null && countCharType(rawPassword, Character::isLowerCase) < minLowerCaseCount)
    {
      results.addError(fieldError(field, getFullCode("password.min-lowercase-chars"),
          DEFAULT_MSG_PREFIX + minLowerCaseCount + " lower case character(s)", minLowerCaseCount));
    }

    Integer minUpperCaseCount = passwordPolicyProps.getMinUpperCaseCount();
    if (minUpperCaseCount != null && countCharType(rawPassword, Character::isUpperCase) < minUpperCaseCount)
    {
      results.addError(fieldError(field, getFullCode("password.min-uppercase-chars"),
          DEFAULT_MSG_PREFIX + minUpperCaseCount + " upper case character(s)", minUpperCaseCount));
    }

    Integer minDigitCount = passwordPolicyProps.getMinDigitCount();
    if (minDigitCount != null && countCharType(rawPassword, Character::isDigit) < minDigitCount)
    {
      results.addError(fieldError(field, getFullCode("password.min-digits"),
          DEFAULT_MSG_PREFIX + minDigitCount + " digit(s)", minDigitCount));
    }

    Integer minSpecialCharsCount = passwordPolicyProps.getMinSpecialCharsCount();
    if (minSpecialCharsCount != null
        && countCharType(rawPassword, not(Character::isLetterOrDigit)) < minSpecialCharsCount)
    {
      results.addError(fieldError(field, getFullCode("password.min-special-chars"),
          DEFAULT_MSG_PREFIX + minSpecialCharsCount + " special character(s)", minSpecialCharsCount));
    }

    log.debug("Validation results on password: {}", results);

    return results;
  }

  private static long countCharType(final String rawPassword, final Predicate<Character> charPredicate)
  {
    if (StringUtils.isEmpty(rawPassword))
    {
      return 0L;
    }

    return rawPassword.chars()
      .mapToObj(c -> (char) c)
      .filter(charPredicate)
      .count();
  }
}
