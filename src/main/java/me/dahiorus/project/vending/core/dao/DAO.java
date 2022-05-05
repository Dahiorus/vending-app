package me.dahiorus.project.vending.core.dao;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.model.AbstractEntity;

@NoRepositoryBean
public interface DAO<E extends AbstractEntity> extends JpaRepository<E, UUID>, JpaSpecificationExecutor<E>
{
  @Transactional(readOnly = true)
  default E read(final UUID id) throws EntityNotFound
  {
    return findById(id).orElseThrow(() -> new EntityNotFound(getDomainClass(), id));
  }

  Class<E> getDomainClass();
}
