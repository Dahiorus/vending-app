package me.dahiorus.project.vending.domain.service.validation;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.ToString;
import me.dahiorus.project.vending.domain.exception.ValidationException;

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
    // avoid duplicate error
    if (objectErrors.stream()
      .noneMatch(
        err -> StringUtils.equals(err.getCode(), e.getCode())))
    {
      objectErrors.add(e);
    }
  }

  public void addError(final FieldValidationError e)
  {
    // avoid duplicate error on a field
    if (fieldErrors.stream()
      .noneMatch(
        err -> StringUtils.equals(err.getField(), e.getField()) && StringUtils.equals(err.getCode(), e.getCode())))
    {
      fieldErrors.add(e);
    }
  }

  public void mergeObjectErrrors(final ValidationResults other)
  {
    other.objectErrors.forEach(this::addError);
  }

  public void mergeFieldErrors(final ValidationResults other)
  {
    other.fieldErrors.forEach(this::addError);
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

  public void throwIfError(final String message)
    throws ValidationException
  {
    if (hasError())
    {
      throw new ValidationException(message, this);
    }
  }
}
