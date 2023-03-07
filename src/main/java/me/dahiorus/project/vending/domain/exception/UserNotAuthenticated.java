package me.dahiorus.project.vending.domain.exception;

public class UserNotAuthenticated extends AppRuntimeException
{
  private static final long serialVersionUID = 5517061511574827656L;

  public UserNotAuthenticated(final String message)
  {
    super(message);
  }
}
