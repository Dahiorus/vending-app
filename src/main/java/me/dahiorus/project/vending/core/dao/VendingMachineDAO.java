package me.dahiorus.project.vending.core.dao;

import me.dahiorus.project.vending.core.model.VendingMachine;

public interface VendingMachineDAO extends DAO<VendingMachine>
{
  @Override
  default Class<VendingMachine> getDomainClass()
  {
    return VendingMachine.class;
  }
}
