package me.dahiorus.project.vending.core.dao;

import me.dahiorus.project.vending.core.model.BinaryData;

public interface BinaryDataDAO extends DAO<BinaryData>
{
  @Override
  default Class<BinaryData> getDomainClass()
  {
    return BinaryData.class;
  }
}
