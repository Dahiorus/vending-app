package me.dahiorus.project.vending.core.dao.impl;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import me.dahiorus.project.vending.core.model.BinaryData;

@Repository
public class BinaryDataDAO extends AbstractDAO<BinaryData>
{
  public BinaryDataDAO(final EntityManager em)
  {
    super(BinaryData.class, em);
  }
}
