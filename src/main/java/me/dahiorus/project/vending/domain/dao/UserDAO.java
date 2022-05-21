package me.dahiorus.project.vending.domain.dao;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import me.dahiorus.project.vending.domain.model.AppUser;

@Repository
public interface UserDAO extends DAO<AppUser>
{
  Optional<AppUser> findByEmail(final String email);
}
