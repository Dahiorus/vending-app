package me.dahiorus.project.vending.core.service.validation;

import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
public class FieldValidationError extends ValidationError
{
  private static final long serialVersionUID = -1907740491728777643L;

  @Getter
  private String field;

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
    CommonError emptyValue = CommonError.EMPTY_VALUE;
    return fieldError(field, emptyValue.code, emptyValue.getDefaultMessageFor(field));
  }

  public static FieldValidationError notUniqueValue(final String field, final Object value)
  {
    CommonError notUnique = CommonError.NOT_UNIQUE;
    return fieldError(field, notUnique.code, notUnique.getDefaultMessageFor(field), value);
  }
}
