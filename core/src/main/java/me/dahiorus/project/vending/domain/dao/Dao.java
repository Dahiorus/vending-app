package me.dahiorus.project.vending.domain.dao;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.model.AbstractEntity;

@NoRepositoryBean
public interface Dao<E extends AbstractEntity>
  extends JpaRepository<E, UUID>, JpaSpecificationExecutor<E>
{
  E read(final UUID id) throws EntityNotFound;

  Class<E> getDomainClass();
}
