package me.dahiorus.project.vending.core.dao;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import me.dahiorus.project.vending.core.model.Stock;

@Repository
public class StockDAO extends AbstractDAO<Stock>
{
  public StockDAO(final EntityManager em)
  {
    super(Stock.class, em);
  }
}