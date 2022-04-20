package me.dahiorus.project.vending.core.exception;

public class AppException extends Exception
{
  private static final long serialVersionUID = 2908550421862741589L;

  public AppException(final String message, final Throwable cause)
  {
    super(message, cause);
  }

  public AppException(final String message)
  {
    super(message);
  }
}
