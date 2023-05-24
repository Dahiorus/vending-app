package me.dahiorus.project.vending.domain.dao;

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import me.dahiorus.project.vending.domain.model.AppUser;

@Transactional(readOnly = true)
public interface UserDAO extends DAO<AppUser>
{
  Optional<AppUser> findByEmail(final String email);
}
