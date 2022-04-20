package me.dahiorus.project.vending.core.manager.impl;

import org.springframework.stereotype.Repository;

import me.dahiorus.project.vending.core.dao.AbstractDAO;
import me.dahiorus.project.vending.core.model.Item;

@Repository
public class ItemManager extends GenericManagerImpl<Item>
{
  public ItemManager(AbstractDAO<Item> dao)
  {
    super(dao);
  }

  @Override
  public Class<Item> getDomainClass()
  {
    return Item.class;
  }
}
