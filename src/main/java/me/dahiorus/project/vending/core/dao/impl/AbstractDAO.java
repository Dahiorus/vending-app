package me.dahiorus.project.vending.core.dao.impl;

import java.util.UUID;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import me.dahiorus.project.vending.core.dao.DAO;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.model.AbstractEntity;

public abstract class AbstractDAO<E extends AbstractEntity> extends SimpleJpaRepository<E, UUID> implements DAO<E>
{
  private final Class<E> domainClass;

  protected AbstractDAO(final Class<E> domainClass, final EntityManager em)
  {
    super(domainClass, em);
    this.domainClass = domainClass;
  }

  @Override
  public E read(final UUID id) throws EntityNotFound
  {
    return findById(id)
      .orElseThrow(() -> new EntityNotFound(getDomainClass(), id));
  }

  @Override
  public Class<E> getDomainClass()
  {
    return domainClass;
  }
}
