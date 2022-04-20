package me.dahiorus.project.vending.core.service.impl;

import static org.springframework.validation.ValidationUtils.invokeValidator;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import me.dahiorus.project.vending.common.HasLogger;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.InvalidData;
import me.dahiorus.project.vending.core.manager.GenericManager;
import me.dahiorus.project.vending.core.model.AbstractEntity;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;
import me.dahiorus.project.vending.core.service.DtoMapper;
import me.dahiorus.project.vending.core.service.DtoService;

public abstract class DtoServiceImpl<E extends AbstractEntity, D extends AbstractDTO<E>, M extends GenericManager<E>>
    implements DtoService<E, D>, HasLogger
{
  protected final M manager;

  protected final DtoMapper dtoMapper;

  protected final DtoValidator<E, D> dtoValidator;

  protected final Class<E> entityClass;

  protected DtoServiceImpl(final M manager, final DtoMapper dtoMapper,
      final DtoValidator<E, D> dtoValidator)
  {
    this.manager = manager;
    this.dtoMapper = dtoMapper;
    this.dtoValidator = dtoValidator;
    entityClass = manager.getDomainClass();
  }

  @Transactional(rollbackFor = InvalidData.class)
  @Override
  public D create(final D dto) throws InvalidData
  {
    getLogger().debug("Creating a new {}: {}", entityClass.getSimpleName(), dto);

    checkErrors(validate(dto), dto);
    E createdEntity = manager.create(dtoMapper.toEntity(dto, entityClass));
    D createdDto = dtoMapper.toDto(createdEntity, getDomainClass());

    getLogger().info("{} created: {}", entityClass.getSimpleName(), createdDto);

    return createdDto;
  }

  @Transactional(readOnly = true, rollbackFor = EntityNotFound.class)
  @Override
  public D read(final UUID id) throws EntityNotFound
  {
    getLogger().debug("Getting {} with ID {}", getDomainClass().getSimpleName(), id);
    E entity = manager.read(id);

    return dtoMapper.toDto(entity, getDomainClass());
  }

  @Transactional(rollbackFor = { EntityNotFound.class, InvalidData.class })
  @Override
  public D update(final UUID id, final D dto) throws EntityNotFound, InvalidData
  {
    getLogger().debug("Updating {} with ID {}: {}", entityClass.getSimpleName(), id, dto);

    E entity = manager.read(id);

    dto.setId(id);
    checkErrors(validate(dto), dto);
    dtoMapper.patchEntity(dto, entity);
    E updatedEntity = manager.update(entity);
    D updatedDto = dtoMapper.toDto(updatedEntity, getDomainClass());

    getLogger().info("{} updated: {}", entityClass.getSimpleName(), updatedDto);

    return updatedDto;
  }

  @Transactional(rollbackFor = EntityNotFound.class)
  @Override
  public void delete(final UUID id) throws EntityNotFound
  {
    getLogger().debug("Deleting {} with ID {}", entityClass.getSimpleName(), id);

    E entity = manager.read(id);
    manager.delete(entity);

    getLogger().info("{} with ID {} deleted", getDomainClass().getSimpleName(), id);
  }

  @Transactional(readOnly = true)
  @Override
  public Page<D> list(final Pageable pageable)
  {
    getLogger().debug("Getting page {} of {}", pageable, entityClass.getSimpleName());

    Page<E> entities = manager.findAll(pageable, null);

    return entities.map(entity -> dtoMapper.toDto(entity, getDomainClass()));
  }

  @Override
  public Optional<D> findById(final UUID id)
  {
    getLogger().debug("Finding one {} with ID {}", entityClass.getSimpleName(), id);

    Optional<E> entity = manager.findOneById(id);

    return Optional.ofNullable(dtoMapper.toDto(entity.orElse(null), getDomainClass()));
  }

  protected Errors validate(final D dto)
  {
    getLogger().debug("Validating {}: {}", entityClass.getSimpleName(), dto);

    Errors errors = new BeanPropertyBindingResult(dto, entityClass.getSimpleName());
    invokeValidator(dtoValidator, dto, errors);

    return errors;
  }

  protected void checkErrors(final Errors errors, final Object target) throws InvalidData
  {
    if (errors.hasErrors())
    {
      throw new InvalidData(errors, target);
    }
  }

  protected abstract Class<D> getDomainClass();
}
