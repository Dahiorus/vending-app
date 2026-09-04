package me.dahiorus.project.vending.domain.validation;

import me.dahiorus.project.vending.domain.exception.InvalidBusinessObject;

public interface Validator<D> {
  ValidationResults buildValidation(D data);

  default void validate(D data) throws InvalidBusinessObject {
    var validation = buildValidation(data);

    validation.throwIfError(data);
  }
}
