package me.dahiorus.project.vending.core.service;

import me.dahiorus.project.vending.core.model.AbstractEntity;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;

public interface DtoMapper
{
  <E extends AbstractEntity, D extends AbstractDTO<E>> E toEntity(D dto, Class<E> targetClass);

  <E extends AbstractEntity, D extends AbstractDTO<E>> D toDto(E entity, Class<D> targetClass);

  <E extends AbstractEntity, D extends AbstractDTO<E>> void patchEntity(D dtoSource, E entityTarget);
}
