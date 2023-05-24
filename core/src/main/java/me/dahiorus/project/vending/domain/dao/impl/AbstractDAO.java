package me.dahiorus.project.vending.domain.dao.impl;

import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import me.dahiorus.project.vending.domain.dao.DAO;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.model.AbstractEntity;

public class AbstractDAO<E extends AbstractEntity> extends SimpleJpaRepository<E, UUID>
  implements DAO<E>
{
  private final Class<E> domainClass;

  public AbstractDAO(final JpaEntityInformation<E, ?> entityInformation,
    final EntityManager entityManager)
  {
    super(entityInformation, entityManager);
    this.domainClass = entityInformation.getJavaType();
  }

  @Override
  public Class<E> getDomainClass()
  {
    return domainClass;
  }

  @Override
  public E read(final UUID id) throws EntityNotFound
  {
    return findById(id).orElseThrow(() -> new EntityNotFound(getDomainClass(), id));
  }
}
