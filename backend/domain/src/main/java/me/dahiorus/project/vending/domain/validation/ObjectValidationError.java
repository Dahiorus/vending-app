package me.dahiorus.project.vending.domain.validation;

import java.io.Serial;
import java.io.Serializable;

public record ObjectValidationError(String code, String defaultMessage, Object... errorArgs)
    implements ValidationError, Serializable {
  @Serial private static final long serialVersionUID = -8799197003814321507L;

  public static ObjectValidationError notUnique(final Object duplicate) {
    return new ObjectValidationError(
        "validation.constraints.object.not_unique",
        "Another object exists with the same unique values",
        duplicate);
  }
}
