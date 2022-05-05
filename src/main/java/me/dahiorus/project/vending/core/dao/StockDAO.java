package me.dahiorus.project.vending.core.dao;

import me.dahiorus.project.vending.core.model.Stock;

public interface StockDAO extends DAO<Stock>
{
  @Override
  default Class<Stock> getDomainClass()
  {
    return Stock.class;
  }
}
