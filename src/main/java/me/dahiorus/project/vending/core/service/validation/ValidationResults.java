package me.dahiorus.project.vending.core.service.validation;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.ToString;
import me.dahiorus.project.vending.core.exception.ValidationException;

@ToString
public class ValidationResults
{
  @Getter
  private Collection<ValidationError> objectErrors = new ArrayList<>(0);

  @Getter
  private Collection<FieldValidationError> fieldErrors = new ArrayList<>(0);

  public boolean hasFieldError(final String field)
  {
    return fieldErrors.stream()
      .anyMatch(error -> StringUtils.equals(field, error.getField()));
  }

  public boolean hasError()
  {
    return !objectErrors.isEmpty() || !fieldErrors.isEmpty();
  }

  public int count()
  {
    return objectErrors.size() + fieldErrors.size();
  }

  public void addError(final ValidationError e)
  {
    objectErrors.add(e);
  }

  public void addError(final FieldValidationError e)
  {
    fieldErrors.add(e);
  }

  public Collection<FieldValidationError> getFieldErrors(final String field)
  {
    return fieldErrors.stream()
      .filter(err -> StringUtils.equals(field, err.getField()))
      .toList();
  }

  public void throwIfError(final Object target, final CrudOperation operation)
      throws ValidationException
  {
    if (hasError())
    {
      throw new ValidationException(operation, target, this);
    }
  }
}
