package me.dahiorus.project.vending.core.dao.impl;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import me.dahiorus.project.vending.core.model.VendingMachine;

@Repository
public class VendingMachineDaoImpl extends AbstractDAO<VendingMachine>
{
  public VendingMachineDaoImpl(final EntityManager em)
  {
    super(VendingMachine.class, em);
  }
}