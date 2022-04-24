package me.dahiorus.project.vending.core.dao;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import me.dahiorus.project.vending.core.model.Item;

@Repository
public class ItemDAO extends AbstractDAO<Item>
{
  public ItemDAO(final EntityManager em)
  {
    super(Item.class, em);
  }
}