package me.dahiorus.project.vending.core.dao.impl;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import me.dahiorus.project.vending.core.dao.UserDAO;
import me.dahiorus.project.vending.core.model.User;
import me.dahiorus.project.vending.core.model.User_;

@Repository
public class UserDaoImpl extends AbstractDAO<User> implements UserDAO
{
  public UserDaoImpl(final EntityManager em)
  {
    super(User.class, em);
  }

  @Override
  public Optional<User> findByEmail(@Nonnull final String email)
  {
    return findOne((root, query, cb) -> cb.equal(root.get(User_.email), email));
  }
}
