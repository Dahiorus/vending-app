package me.dahiorus.project.vending.domain.service.validation;

import javax.annotation.Nonnull;

import me.dahiorus.project.vending.domain.model.AbstractEntity;
import me.dahiorus.project.vending.domain.model.dto.AbstractDTO;

public interface DtoValidator<D extends AbstractDTO<? extends AbstractEntity>>
{
  @Nonnull
  ValidationResults validate(@Nonnull D dto);
}
