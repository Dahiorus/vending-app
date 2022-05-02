package me.dahiorus.project.vending.core.dao;

import java.util.Optional;

import org.springframework.data.repository.NoRepositoryBean;

import me.dahiorus.project.vending.core.model.AppUser;

@NoRepositoryBean
public interface UserDAO extends DAO<AppUser>
{
  Optional<AppUser> findByEmail(final String email);
}
