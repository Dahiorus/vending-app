package me.dahiorus.project.vending.core.dao;

import java.util.Optional;

import me.dahiorus.project.vending.core.model.AppRole;

public interface RoleDAO extends DAO<AppRole>
{
  Optional<AppRole> findByName(String name);

  @Override
  default Class<AppRole> getDomainClass()
  {
    return AppRole.class;
  }
}
