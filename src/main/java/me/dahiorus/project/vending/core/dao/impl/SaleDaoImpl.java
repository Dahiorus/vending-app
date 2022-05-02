package me.dahiorus.project.vending.core.dao.impl;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import me.dahiorus.project.vending.core.model.Sale;

@Repository
public class SaleDaoImpl extends AbstractDAO<Sale>
{
  public SaleDaoImpl(final EntityManager em)
  {
    super(Sale.class, em);
  }
}
