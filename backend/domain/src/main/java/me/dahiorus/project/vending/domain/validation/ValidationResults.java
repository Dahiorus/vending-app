package me.dahiorus.project.vending.domain.validation;

import java.util.LinkedHashSet;
import java.util.Set;
import me.dahiorus.project.vending.domain.exception.InvalidBusinessObject;

public record ValidationResults(Set<ValidationError> validationErrors) {
  public static ValidationResults validationResults() {
    return new ValidationResults(new LinkedHashSet<>());
  }

  public void addError(ValidationError error) {
    validationErrors.add(error);
  }

  public void throwIfError(Object target) throws InvalidBusinessObject {
    if (!validationErrors.isEmpty()) {
      throw new InvalidBusinessObject(target, validationErrors);
    }
  }
}
