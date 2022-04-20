package me.dahiorus.project.vending.core.manager.impl;

import org.springframework.stereotype.Repository;

import me.dahiorus.project.vending.core.dao.AbstractDAO;
import me.dahiorus.project.vending.core.model.CommunicationModule;

@Repository
public class CommunicationModuleManager extends GenericManagerImpl<CommunicationModule>
{
  public CommunicationModuleManager(AbstractDAO<CommunicationModule> dao)
  {
    super(dao);
  }

  @Override
  public Class<CommunicationModule> getDomainClass()
  {
    return CommunicationModule.class;
  }
}
