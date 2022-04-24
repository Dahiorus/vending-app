package com.dahiorus.project.vending.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import org.springframework.data.jpa.domain.Specification;

import me.dahiorus.project.vending.core.model.AbstractEntity;
import me.dahiorus.project.vending.core.service.validation.FieldValidationError;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;

public class TestUtils
{
  public static <T extends AbstractEntity> Specification<T> anySpec()
  {
    return any();
  }

  public static void assertHasExactlyFieldErrors(final ValidationResults results, final String field,
      final String... codes)
  {
    assertThat(results.getFieldErrors(field)).extracting(FieldValidationError::getCode)
      .containsExactly(codes);
  }

  private TestUtils()
  {
    // util class
  }
}
