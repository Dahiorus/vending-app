package me.dahiorus.project.vending.domain.exception;

import static java.lang.String.format;

import java.util.Set;
import me.dahiorus.project.vending.domain.validation.ValidationError;

public class InvalidBusinessObject extends RuntimeException {
  private final Set<ValidationError> errors;

  public InvalidBusinessObject(Object invalidData, Set<ValidationError> errors) {
    super(format("%d error(s) found in invalid business object: %s", errors.size(), invalidData));
    this.errors = errors;
  }

  public Set<ValidationError> getErrors() {
    return errors;
  }
}
