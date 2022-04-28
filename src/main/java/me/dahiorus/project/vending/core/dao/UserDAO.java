package me.dahiorus.project.vending.core.dao;

import java.util.Optional;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import me.dahiorus.project.vending.core.model.User;
import me.dahiorus.project.vending.core.model.User_;

@Repository
public class UserDAO extends AbstractDAO<User>
{
  public UserDAO(final EntityManager em)
  {
    super(User.class, em);
  }

  public Optional<User> findByEmail(final String email)
  {
    return findOne((root, query, cb) -> cb.equal(root.get(User_.email), email));
  }
}
