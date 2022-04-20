package me.dahiorus.project.vending.core.manager;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.model.AbstractEntity;

public interface GenericManager<E extends AbstractEntity>
{
  E create(E entity);

  E read(UUID id) throws EntityNotFound;

  E update(E entity);

  void delete(E entity);

  boolean exists(UUID id);

  Page<E> findAll(Pageable pageable, Specification<E> specification);

  Optional<E> findOneById(UUID id);

  Optional<E> findOne(Specification<E> specification);

  Class<E> getDomainClass();
}
