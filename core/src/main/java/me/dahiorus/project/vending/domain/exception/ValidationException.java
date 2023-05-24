package me.dahiorus.project.vending.domain.exception;

import java.util.Collection;

import lombok.Getter;
import me.dahiorus.project.vending.domain.service.validation.CrudOperation;
import me.dahiorus.project.vending.domain.service.validation.FieldValidationError;
import me.dahiorus.project.vending.domain.service.validation.ValidationError;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;

public class ValidationException extends AppException
{
  private static final long serialVersionUID = 6940407639911567414L;

  @Getter
  private final Collection<ValidationError> globalErrors;

  @Getter
  private final Collection<FieldValidationError> fieldErrors;

  @Getter
  private final int count;

  public ValidationException(final CrudOperation operation, final Object target,
    final ValidationResults validationResults)
  {
    this(operation + ": " + validationResults.count() + " error(s) found on " + target, validationResults);
  }

  public ValidationException(final String message, final ValidationResults validationResults)
  {
    super(message);
    this.fieldErrors = validationResults.getFieldErrors();
    this.globalErrors = validationResults.getObjectErrors();
    this.count = validationResults.count();
  }
}
