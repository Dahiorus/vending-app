package me.dahiorus.project.vending.web.exception;

public class UnparsableToken extends TokenException
{
  private static final long serialVersionUID = 4048788159425550930L;

  public UnparsableToken(final String message)
  {
    super(message);
  }

  public UnparsableToken(final String message, final Throwable cause)
  {
    super(message, cause);
  }
}
