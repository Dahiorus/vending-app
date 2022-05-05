package me.dahiorus.project.vending.core.dao;

import me.dahiorus.project.vending.core.model.Item;

public interface ItemDAO extends DAO<Item>
{
  @Override
  default Class<Item> getDomainClass()
  {
    return Item.class;
  }
}
