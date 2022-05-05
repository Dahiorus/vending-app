package me.dahiorus.project.vending.core.dao;

import java.util.Optional;

import me.dahiorus.project.vending.core.model.AppUser;

public interface UserDAO extends DAO<AppUser>
{
  Optional<AppUser> findByEmail(final String email);

  @Override
  default Class<AppUser> getDomainClass()
  {
    return AppUser.class;
  }
}
