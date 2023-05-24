package me.dahiorus.project.vending.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.dahiorus.project.vending.domain.service.validation.FieldValidationError;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationTestUtils
{
  public static ValidationResults successResults()
  {
    return new ValidationResults();
  }

  public static void assertNoFieldError(final ValidationResults results, final String field)
  {
    assertThat(results.getFieldErrors(field)).isEmpty();
  }

  public static void assertHasExactlyFieldErrors(final ValidationResults results,
    final String field, final String... codes)
  {
    assertThat(results.getFieldErrors(field))
      .as("Expecting %s error(s) on field '%s'", Arrays.asList(codes), field)
      .isNotEmpty()
      .extracting(FieldValidationError::getCode)
      .containsExactlyInAnyOrder(codes);
  }
}
