package me.dahiorus.project.vending.core.dao.impl;

import java.util.Optional;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import me.dahiorus.project.vending.core.dao.RoleDAO;
import me.dahiorus.project.vending.core.model.AppRole;
import me.dahiorus.project.vending.core.model.AppRole_;

@Repository
public class RoleDaoImpl extends AbstractDAO<AppRole> implements RoleDAO
{
  public RoleDaoImpl(final EntityManager em)
  {
    super(AppRole.class, em);
  }

  @Override
  public Optional<AppRole> findByName(final String name)
  {
    return findOne((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(AppRole_.name), name));
  }
}
