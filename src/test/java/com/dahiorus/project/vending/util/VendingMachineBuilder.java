package com.dahiorus.project.vending.util;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.dahiorus.project.vending.core.model.ItemType;
import me.dahiorus.project.vending.core.model.VendingMachine;

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

  public VendingMachine build()
  {
    return machine;
  }
}
