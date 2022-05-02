package me.dahiorus.project.vending.core.dao.impl;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import me.dahiorus.project.vending.core.dao.UserDAO;
import me.dahiorus.project.vending.core.model.AppUser;
import me.dahiorus.project.vending.core.model.AppUser_;

@Repository
public class UserDaoImpl extends AbstractDAO<AppUser> implements UserDAO
{
  public UserDaoImpl(final EntityManager em)
  {
    super(AppUser.class, em);
  }

  @Override
  public Optional<AppUser> findByEmail(@Nonnull final String email)
  {
    return findOne((root, query, cb) -> cb.equal(root.get(AppUser_.email), email));
  }
}
