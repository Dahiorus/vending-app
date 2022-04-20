package me.dahiorus.project.vending.core.exception;

public class AppRuntimeException extends RuntimeException
{
  private static final long serialVersionUID = -3675934779060967108L;

  public AppRuntimeException(final String message, final Throwable cause)
  {
    super(message, cause);
  }

  public AppRuntimeException(final String message)
  {
    super(message);
  }
}
