package me.dahiorus.project.vending.core.dao;

import java.util.UUID;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.model.AbstractEntity;

public abstract class AbstractDAO<E extends AbstractEntity> extends SimpleJpaRepository<E, UUID>
{
  private final Class<E> domainClass;

  protected AbstractDAO(final Class<E> domainClass, final EntityManager em)
  {
    super(domainClass, em);
    this.domainClass = domainClass;
  }

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
