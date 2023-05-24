package me.dahiorus.project.vending.domain.service.validation;

import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
public class FieldValidationError extends ValidationError
{
  private static final long serialVersionUID = -1907740491728777643L;

  @Getter
  private final String field;

  private FieldValidationError(final String field, final String code, final String defaultMessage,
    final Object[] errorArgs)
  {
    super(code, defaultMessage, errorArgs);
    this.field = field;
  }

  public static FieldValidationError fieldError(final String field, final String code, final String defaultMessage,
    final Object... errorArgs)
  {
    return new FieldValidationError(field, code, defaultMessage, errorArgs);
  }

  public static FieldValidationError emptyOrNullValue(final String field)
  {
    return fieldError(field, CommonError.EMPTY_VALUE.code, field + " is mandatory");
  }

  public static FieldValidationError notUniqueValue(final String field, final Object value)
  {
    return fieldError(field, CommonError.NOT_UNIQUE.code, field + " must be unique", value);
  }

  public static FieldValidationError maxLength(final String field, final int length)
  {
    return fieldError(field, CommonError.MAX_LENGTH.code, field + " must have a max length of " + length,
      length);
  }
}
