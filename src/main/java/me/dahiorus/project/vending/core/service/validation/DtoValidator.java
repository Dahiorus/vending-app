package me.dahiorus.project.vending.core.service.validation;

import javax.annotation.Nonnull;

import me.dahiorus.project.vending.core.model.AbstractEntity;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;

public interface DtoValidator<D extends AbstractDTO<? extends AbstractEntity>>
{
  @Nonnull
  ValidationResults validate(@Nonnull D dto);
}
