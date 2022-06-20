package me.dahiorus.project.vending.domain.dao;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import me.dahiorus.project.vending.domain.model.AppUser;

@Transactional(readOnly = true)
@Repository
public interface UserDAO extends DAO<AppUser>
{
  Optional<AppUser> findByEmail(final String email);
}
