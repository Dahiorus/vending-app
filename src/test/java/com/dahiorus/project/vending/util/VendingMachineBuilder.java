package com.dahiorus.project.vending.util;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.dahiorus.project.vending.core.model.CardSystemStatus;
import me.dahiorus.project.vending.core.model.ChangeSystemStatus;
import me.dahiorus.project.vending.core.model.ItemType;
import me.dahiorus.project.vending.core.model.PowerStatus;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.model.WorkingStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VendingMachineBuilder
{
  private VendingMachine machine = new VendingMachine();

  public static VendingMachineBuilder builder()
  {
    return new VendingMachineBuilder();
  }

  public VendingMachineBuilder itemType(final ItemType type)
  {
    machine.setType(type);
    return this;
  }

  public VendingMachineBuilder id(final UUID id)
  {
    machine.setId(id);
    return this;
  }

  public VendingMachineBuilder powerStatus(final PowerStatus powerStatus)
  {
    machine.setPowerStatus(powerStatus);
    return this;
  }

  public VendingMachineBuilder workingStatus(final WorkingStatus workingStatus)
  {
    machine.setWorkingStatus(workingStatus);
    return this;
  }

  public VendingMachineBuilder rfidStatus(final CardSystemStatus rfidStatus)
  {
    machine.setRfidStatus(rfidStatus);
    return this;
  }

  public VendingMachineBuilder smartCardStatus(final CardSystemStatus smartCardStatus)
  {
    machine.setSmartCardStatus(smartCardStatus);
    return this;
  }

  public VendingMachineBuilder changeMoneyStatus(final ChangeSystemStatus changeMoneyStatus)
  {
    machine.setChangeMoneyStatus(changeMoneyStatus);
    return this;
  }

  public VendingMachine build()
  {
    return machine;
  }
}
