package me.dahiorus.project.vending.core.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import me.dahiorus.project.vending.core.model.AbstractEntity;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;
import me.dahiorus.project.vending.core.service.DtoMapper;

@Component
public class DtoMapperImpl implements DtoMapper
{
  private static final Logger logger = LogManager.getLogger(DtoMapperImpl.class);

  private final ModelMapper mapper;

  public DtoMapperImpl()
  {
    mapper = new ModelMapper();
  }

  @Override
  public <E extends AbstractEntity, D extends AbstractDTO<E>> E toEntity(final D dto, final Class<E> targetClass)
  {
    logger.debug("Converting {} to instance of {}", dto, targetClass.getSimpleName());

    if (dto == null)
    {
      return null;
    }

    return mapper.map(dto, targetClass);
  }

  @Override
  public <E extends AbstractEntity, D extends AbstractDTO<E>> D toDto(final E entity, final Class<D> targetClass)
  {
    logger.debug("Converting {} to instance of {}", entity, targetClass.getSimpleName());

    if (entity == null)
    {
      return null;
    }

    return mapper.map(entity, targetClass);
  }

  @Override
  public <E extends AbstractEntity, D extends AbstractDTO<E>> void patchEntity(final D dtoSource, final E entityTarget)
  {
    if (dtoSource == null)
    {
      return;
    }

    mapper.map(dtoSource, entityTarget);
  }
}
