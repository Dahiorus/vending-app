package me.dahiorus.project.vending.core.exception;

public class ItemMissing extends AppException
{
  private static final long serialVersionUID = -4305916488885282178L;

  public ItemMissing(final String message)
  {
    super(message);
  }
}
