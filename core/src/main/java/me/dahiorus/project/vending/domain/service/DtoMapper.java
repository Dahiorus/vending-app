package me.dahiorus.project.vending.domain.service;

import me.dahiorus.project.vending.domain.model.AbstractEntity;
import me.dahiorus.project.vending.domain.model.dto.AbstractDto;

public interface DtoMapper
{
  <E extends AbstractEntity, D extends AbstractDto<E>> E toEntity(D dto, Class<E> targetClass);

  <E extends AbstractEntity, D extends AbstractDto<E>> D toDto(E entity, Class<D> targetClass);

  <E extends AbstractEntity, D extends AbstractDto<E>> void patchEntity(D dtoSource, E entityTarget);
}
