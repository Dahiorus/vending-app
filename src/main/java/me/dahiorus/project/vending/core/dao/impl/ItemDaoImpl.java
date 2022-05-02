package me.dahiorus.project.vending.core.dao.impl;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import me.dahiorus.project.vending.core.model.Item;

@Repository
public class ItemDaoImpl extends AbstractDAO<Item>
{
  public ItemDaoImpl(final EntityManager em)
  {
    super(Item.class, em);
  }
}