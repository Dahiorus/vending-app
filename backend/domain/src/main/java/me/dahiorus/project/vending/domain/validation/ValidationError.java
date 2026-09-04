package me.dahiorus.project.vending.domain.validation;

public sealed interface ValidationError permits ObjectValidationError, FieldValidationError {
  String code();

  String defaultMessage();

  Object[] errorArgs();
}
