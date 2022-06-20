package me.dahiorus.project.vending.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import java.util.Arrays;

import org.springframework.data.jpa.domain.Specification;

import io.swagger.v3.core.util.Json;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.dahiorus.project.vending.domain.model.AbstractEntity;
import me.dahiorus.project.vending.domain.service.validation.FieldValidationError;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtils
{
  public static String jsonValue(final Object object) throws Exception
  {
    return Json.pretty(object);
  }

  public static <T extends AbstractEntity> Specification<T> anySpec()
  {
    return any();
  }

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
