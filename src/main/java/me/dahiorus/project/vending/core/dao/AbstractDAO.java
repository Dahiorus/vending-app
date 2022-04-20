package me.dahiorus.project.vending.core.dao;

import java.util.UUID;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import me.dahiorus.project.vending.core.model.AbstractEntity;

@Transactional(propagation = Propagation.SUPPORTS)
public abstract class AbstractDAO<E extends AbstractEntity> extends SimpleJpaRepository<E, UUID>
{
  protected AbstractDAO(Class<E> domainClass, EntityManager em)
  {
    super(domainClass, em);
  }
}
