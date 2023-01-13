package me.dahiorus.project.vending.domain.exception;

public class VendingMachineNotWorking extends AppException
{
  private static final long serialVersionUID = -6343459314000702938L;

  public VendingMachineNotWorking(final String message)
  {
    super(message);
  }
}
