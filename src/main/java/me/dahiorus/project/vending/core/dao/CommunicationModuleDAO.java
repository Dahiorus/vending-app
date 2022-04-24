package me.dahiorus.project.vending.core.dao;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import me.dahiorus.project.vending.core.model.CommunicationModule;

@Repository
public class CommunicationModuleDAO extends AbstractDAO<CommunicationModule>
{
  public CommunicationModuleDAO(final EntityManager em)
  {
    super(CommunicationModule.class, em);
  }
}