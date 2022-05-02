package me.dahiorus.project.vending.core.dao.impl;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import me.dahiorus.project.vending.core.model.Stock;

@Repository
public class StockDaoImpl extends AbstractDAO<Stock>
{
  public StockDaoImpl(final EntityManager em)
  {
    super(Stock.class, em);
  }
}