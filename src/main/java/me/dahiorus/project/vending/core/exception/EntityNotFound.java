package me.dahiorus.project.vending.core.exception;

import java.util.UUID;

import me.dahiorus.project.vending.core.model.AbstractEntity;

public class EntityNotFound extends AppException
{
  private static final long serialVersionUID = 3978489411112889438L;

  public EntityNotFound(final Class<? extends AbstractEntity> entityClass, final UUID id)
  {
    super("No " + entityClass.getSimpleName() + " found with ID " + id);
  }
}
