package me.dahiorus.project.vending.web.exception;

public class UncreatableToken extends TokenException
{
  private static final long serialVersionUID = -6469989744968236198L;

  public UncreatableToken(final String message, final Throwable cause)
  {
    super(message, cause);
  }

  public UncreatableToken(final String message)
  {
    super(message);
  }
}
