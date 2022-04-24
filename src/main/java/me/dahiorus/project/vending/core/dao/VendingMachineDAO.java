package me.dahiorus.project.vending.core.dao;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import me.dahiorus.project.vending.core.model.VendingMachine;

@Repository
public class VendingMachineDAO extends AbstractDAO<VendingMachine>
{
  public VendingMachineDAO(final EntityManager em)
  {
    super(VendingMachine.class, em);
  }
}