package me.dahiorus.project.vending.web.exception;

public class InvalidTokenCreation extends TokenException
{
  private static final long serialVersionUID = -6469989744968236198L;

  public InvalidTokenCreation(final String message, final Throwable cause)
  {
    super(message, cause);
  }

  public InvalidTokenCreation(final String message)
  {
    super(message);
  }
}
