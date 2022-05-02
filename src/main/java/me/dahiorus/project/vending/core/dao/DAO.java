package me.dahiorus.project.vending.core.dao;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.model.AbstractEntity;

public interface DAO<E extends AbstractEntity> extends JpaRepository<E, UUID>, JpaSpecificationExecutor<E>
{
  E read(UUID id) throws EntityNotFound;

  Class<E> getDomainClass();
}
