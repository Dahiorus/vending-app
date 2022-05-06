package me.dahiorus.project.vending.core.dao;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import me.dahiorus.project.vending.core.model.AppUser;

@Repository
public interface UserDAO extends DAO<AppUser>
{
  Optional<AppUser> findByEmail(final String email);
}
