package me.dahiorus.project.vending.core.exception;

public class UserNotAuthenticated extends AppException
{
  private static final long serialVersionUID = 5517061511574827656L;

  public UserNotAuthenticated(final String message)
  {
    super(message);
  }
}
