package me.dahiorus.project.vending.domain.service.validation;

import jakarta.annotation.Nonnull;
import me.dahiorus.project.vending.domain.model.AbstractEntity;
import me.dahiorus.project.vending.domain.model.dto.AbstractDto;

public interface DtoValidator<D extends AbstractDto<? extends AbstractEntity>>
{
  @Nonnull
  ValidationResults validate(@Nonnull D dto);
}
