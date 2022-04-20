package me.dahiorus.project.vending.core.manager.impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import me.dahiorus.project.vending.core.dao.AbstractDAO;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.manager.GenericManager;
import me.dahiorus.project.vending.core.model.AbstractEntity;

public abstract class GenericManagerImpl<E extends AbstractEntity> implements GenericManager<E>
{
  private final AbstractDAO<E> dao;

  protected GenericManagerImpl(final AbstractDAO<E> dao)
  {
    this.dao = dao;
  }

  @Transactional
  @Override
  public E create(final E entity)
  {
    return dao.save(entity);
  }

  @Transactional
  @Override
  public E read(final UUID id) throws EntityNotFound
  {
    return findOneById(id)
      .orElseThrow(() -> new EntityNotFound(getDomainClass(), id));
  }

  @Transactional
  @Override
  public E update(final E entity)
  {
    return dao.save(entity);
  }

  @Transactional
  @Override
  public void delete(final E entity)
  {
    dao.delete(entity);
  }

  @Transactional
  @Override
  public boolean exists(final UUID id)
  {
    return dao.existsById(id);
  }

  @Transactional
  @Override
  public Page<E> findAll(final Pageable pageable, final Specification<E> specification)
  {
    return dao.findAll(specification, pageable);
  }

  @Transactional
  @Override
  public Optional<E> findOneById(final UUID id)
  {
    return dao.findById(id);
  }

  @Transactional
  @Override
  public Optional<E> findOne(final Specification<E> specification)
  {
    return dao.findOne(specification);
  }
}
