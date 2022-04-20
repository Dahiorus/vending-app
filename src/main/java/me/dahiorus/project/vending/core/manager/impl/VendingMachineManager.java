package me.dahiorus.project.vending.core.manager.impl;

import org.springframework.stereotype.Repository;

import me.dahiorus.project.vending.core.dao.AbstractDAO;
import me.dahiorus.project.vending.core.model.VendingMachine;

@Repository
public class VendingMachineManager extends GenericManagerImpl<VendingMachine>
{
  public VendingMachineManager(final AbstractDAO<VendingMachine> dao)
  {
    super(dao);
  }

  @Override
  public Class<VendingMachine> getDomainClass()
  {
    return VendingMachine.class;
  }
}
