package me.dahiorus.project.vending.core.service.validation.impl;

import static me.dahiorus.project.vending.util.TestUtils.assertHasExactlyFieldErrors;
import static me.dahiorus.project.vending.util.TestUtils.assertNoFieldError;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import me.dahiorus.project.vending.core.config.PasswordPolicyProperties;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;

class PasswordValidatorImplTest
{
  PasswordPolicyProperties passwordPolicyProperties;

  PasswordValidatorImpl validator;

  @BeforeEach
  void setUp()
  {
    passwordPolicyProperties = new PasswordPolicyProperties();
    passwordPolicyProperties.setMinLength(12);
    passwordPolicyProperties.setMaxLength(24);
    passwordPolicyProperties.setMinSpecialCharsCount(1);

    validator = new PasswordValidatorImpl(passwordPolicyProperties);
  }

  @Test
  void isValid()
  {
    testOn("Azertyuiop123&", true);
  }

  @Test
  void mustHaveMinLength()
  {
    testOn("Secret123&", true, "validation.constraints.password.min-length");
  }

  @Test
  void mustHaveMaxLength()
  {
    testOn(randomAlphanumeric(24) + "Az1&", true, "validation.constraints.password.max-length");
  }

  @Test
  void mustHaveLowerCaseChar()
  {
    testOn("AZERTYIOP123&", true, "validation.constraints.password.min-lowercase-chars");
  }

  @Test
  void mustHaveUpperCaseChar()
  {
    testOn("azertyuiop123&", true, "validation.constraints.password.min-uppercase-chars");
  }

  @Test
  void mustHaveDigit()
  {
    testOn(randomAlphabetic(13) + '&', true, "validation.constraints.password.min-digits");
  }

  @Test
  void mustHaveSpecialChar()
  {
    testOn(randomAlphanumeric(13) + "Ééù2", true, "validation.constraints.password.min-special-chars");
  }

  @Test
  void hasMultipleErrors()
  {
    testOn("secret", false, "validation.constraints.password.min-length",
        "validation.constraints.password.min-uppercase-chars", "validation.constraints.password.min-digits",
        "validation.constraints.password.min-special-chars");
  }

  @ParameterizedTest
  @NullAndEmptySource
  void emptyPasswordButIsMandatory(final String password)
  {
    testOn(password, true, "validation.constraints.empty_value", "validation.constraints.password.min-length",
        "validation.constraints.password.min-lowercase-chars", "validation.constraints.password.min-uppercase-chars",
        "validation.constraints.password.min-digits", "validation.constraints.password.min-special-chars");
  }

  @ParameterizedTest
  @NullAndEmptySource
  void emptyPasswordButIsOptional(final String password)
  {
    testOn(password, false);
  }

  @Test
  void emptyPasswordPolicy()
  {
    PasswordPolicyProperties props = new PasswordPolicyProperties();
    props.setMinLength(null);
    props.setMaxLength(null);
    props.setMinLowerCaseCount(null);
    props.setMinUpperCaseCount(null);
    props.setMinDigitCount(null);
    props.setMinSpecialCharsCount(null);

    validator = new PasswordValidatorImpl(props);

    testOn("Secret", true);
  }

  private void testOn(final String rawPassword, final boolean mandatory, final String... expectedErrorCodes)
  {
    String field = "pwdField";
    ValidationResults results = validator.validate(field, rawPassword, mandatory);

    if (ArrayUtils.isEmpty(expectedErrorCodes))
    {
      assertNoFieldError(results, field);
    }
    else
    {
      assertHasExactlyFieldErrors(results, field, expectedErrorCodes);
    }
  }
}
