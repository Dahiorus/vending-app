package me.dahiorus.project.vending.web.exception;

import me.dahiorus.project.vending.domain.exception.AppException;

public abstract class TokenException extends AppException
{
  private static final long serialVersionUID = 3712340443274600007L;

  protected TokenException(final String message, final Throwable cause)
  {
    super(message, cause);
  }

  protected TokenException(final String message)
  {
    super(message);
  }
}
