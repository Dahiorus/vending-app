package me.dahiorus.project.vending.core.service.validation;

import javax.annotation.Nonnull;

import me.dahiorus.project.vending.core.model.AbstractEntity;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;

public interface DtoValidator<E extends AbstractEntity, D extends AbstractDTO<E>>
{
  @Nonnull
  ValidationResults validate(@Nonnull D dto);
}
