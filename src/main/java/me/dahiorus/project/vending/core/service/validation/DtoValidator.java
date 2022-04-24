package me.dahiorus.project.vending.core.service.validation;

import me.dahiorus.project.vending.core.model.AbstractEntity;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;

public interface DtoValidator<E extends AbstractEntity, D extends AbstractDTO<E>>
{
  ValidationResults validate(D dto);
}
