package me.dahiorus.project.vending.core.dao;

import java.util.Optional;

import me.dahiorus.project.vending.core.model.User;

public interface UserDAO extends DAO<User>
{
  Optional<User> findByEmail(final String email);
}
