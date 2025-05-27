package me.dahiorus.project.vending.domain.validation;

import java.io.Serial;
import java.io.Serializable;

public record FieldValidationError(
    String field, String code, String defaultMessage, Object... errorArgs)
    implements ValidationError, Serializable {
  @Serial private static final long serialVersionUID = -1907740491728777643L;

  public static FieldValidationError notUniqueValue(final String field, final Object value) {
    return new FieldValidationError(
        field, "validation.constraints.field.not_unique", field + " must be unique", value);
  }
}
