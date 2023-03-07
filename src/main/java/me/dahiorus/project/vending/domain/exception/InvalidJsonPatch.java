package me.dahiorus.project.vending.domain.exception;

public class InvalidJsonPatch extends AppRuntimeException
{
  private static final long serialVersionUID = -7349810992370883635L;

  public InvalidJsonPatch(final String message, final Throwable cause)
  {
    super(message, cause);
  }

  public InvalidJsonPatch(final String message)
  {
    super(message);
  }
}
