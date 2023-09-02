package me.dahiorus.project.vending.domain.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.model.AbstractEntity;
import me.dahiorus.project.vending.domain.model.dto.AbstractDto;
import me.dahiorus.project.vending.domain.service.DtoMapper;

@Log4j2
@Component
public class DtoMapperImpl implements DtoMapper
{
  private final ModelMapper mapper;

  public DtoMapperImpl()
  {
    mapper = new ModelMapper();
  }

  @Override
  public <E extends AbstractEntity, D extends AbstractDto<E>> E toEntity(final D dto,
    final Class<E> targetClass)
  {
    log.debug("Converting {} to instance of {}", dto, targetClass.getSimpleName());

    if (dto == null)
    {
      return null;
    }

    return mapper.map(dto, targetClass);
  }

  @Override
  public <E extends AbstractEntity, D extends AbstractDto<E>> D toDto(final E entity,
    final Class<D> targetClass)
  {
    log.debug("Converting {} to instance of {}", entity, targetClass.getSimpleName());

    if (entity == null)
    {
      return null;
    }

    return mapper.map(entity, targetClass);
  }

  @Override
  public <E extends AbstractEntity, D extends AbstractDto<E>> void patchEntity(final D dtoSource,
    final E entityTarget)
  {
    if (dtoSource == null)
    {
      return;
    }

    mapper.map(dtoSource, entityTarget);
  }
}
