package me.dahiorus.project.vending.domain.service.validation;

public enum CrudOperation
{
  CREATE,
  UPDATE,
  DELETE;
  
  public boolean isCreateOrUpdate()
  {
    return this == CREATE || this == UPDATE;
  }
}
