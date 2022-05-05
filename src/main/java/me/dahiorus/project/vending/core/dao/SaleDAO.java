package me.dahiorus.project.vending.core.dao;

import me.dahiorus.project.vending.core.model.Sale;

public interface SaleDAO extends DAO<Sale>
{
  @Override
  default Class<Sale> getDomainClass()
  {
    return Sale.class;
  }
}
