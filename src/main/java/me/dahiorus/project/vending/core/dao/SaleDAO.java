package me.dahiorus.project.vending.core.dao;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import me.dahiorus.project.vending.core.model.Sale;

@Repository
public class SaleDAO extends AbstractDAO<Sale>
{
  public SaleDAO(final EntityManager em)
  {
    super(Sale.class, em);
  }
}
