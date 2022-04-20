package me.dahiorus.project.vending.core.exception;

import org.springframework.validation.Errors;

public class InvalidData extends AppException
{
  private static final long serialVersionUID = 6940407639911567414L;

  private final Errors errors;

  private final Object target;

  public InvalidData(final Errors errors, final Object target)
  {
    super("Invalid data given " + target + ": " + errors.getErrorCount() + " error(s) found");
    this.errors = errors;
    this.target = target;
  }

  public Errors getErrors()
  {
    return errors;
  }

  public Object getTarget()
  {
    return target;
  }
}
