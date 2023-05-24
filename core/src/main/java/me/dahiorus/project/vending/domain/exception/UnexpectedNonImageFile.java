package me.dahiorus.project.vending.domain.exception;

public class UnexpectedNonImageFile extends AppRuntimeException
{
  private static final long serialVersionUID = 8546910312699343555L;

  public UnexpectedNonImageFile(final String message, final Throwable cause)
  {
    super(message, cause);
  }

  public UnexpectedNonImageFile(final String message)
  {
    super(message);
  }
}
