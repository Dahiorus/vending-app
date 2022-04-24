package me.dahiorus.project.vending.core.service.impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import me.dahiorus.project.vending.common.HasLogger;
import me.dahiorus.project.vending.core.dao.AbstractDAO;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.AbstractEntity;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;
import me.dahiorus.project.vending.core.service.DtoMapper;
import me.dahiorus.project.vending.core.service.DtoService;
import me.dahiorus.project.vending.core.service.validation.CrudOperation;
import me.dahiorus.project.vending.core.service.validation.DtoValidator;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;

public abstract class DtoServiceImpl<E extends AbstractEntity, D extends AbstractDTO<E>, DAO extends AbstractDAO<E>>
    implements DtoService<E, D>, HasLogger
{
  protected final DAO dao;

  protected final DtoMapper dtoMapper;

  protected final Optional<DtoValidator<E, D>> dtoValidator;

  protected final Class<E> entityClass;

  protected DtoServiceImpl(final DAO dao, final DtoMapper dtoMapper, final DtoValidator<E, D> dtoValidator)
  {
    this.dao = dao;
    this.dtoMapper = dtoMapper;
    this.dtoValidator = Optional.ofNullable(dtoValidator);
    entityClass = dao.getDomainClass();
  }

  @Transactional(rollbackFor = ValidationException.class)
  @Override
  public D create(final D dto) throws ValidationException
  {
    getLogger().debug("Creating a new {}: {}", entityClass.getSimpleName(), dto);

    validate(dto, CrudOperation.CREATE);
    E createdEntity = dao.save(dtoMapper.toEntity(dto, entityClass));
    D createdDto = dtoMapper.toDto(createdEntity, getDomainClass());

    getLogger().info("{} created: {}", entityClass.getSimpleName(), createdDto);

    return createdDto;
  }

  @Transactional(readOnly = true, rollbackFor = EntityNotFound.class)
  @Override
  public D read(final UUID id) throws EntityNotFound
  {
    getLogger().debug("Getting {} with ID {}", getDomainClass().getSimpleName(), id);
    E entity = dao.read(id);

    return dtoMapper.toDto(entity, getDomainClass());
  }

  @Transactional(rollbackFor = { EntityNotFound.class, ValidationException.class })
  @Override
  public D update(final UUID id, final D dto) throws EntityNotFound, ValidationException
  {
    getLogger().debug("Updating {} with ID {}: {}", entityClass.getSimpleName(), id, dto);

    E entity = dao.read(id);

    dto.setId(id);
    validate(dto, CrudOperation.UPDATE);
    dtoMapper.patchEntity(dto, entity);
    E updatedEntity = dao.save(entity);
    D updatedDto = dtoMapper.toDto(updatedEntity, getDomainClass());

    getLogger().info("{} updated: {}", entityClass.getSimpleName(), updatedDto);

    return updatedDto;
  }

  @Transactional(rollbackFor = EntityNotFound.class)
  @Override
  public void delete(final UUID id) throws EntityNotFound
  {
    getLogger().debug("Deleting {} with ID {}", entityClass.getSimpleName(), id);

    E entity = dao.read(id);
    dao.delete(entity);

    getLogger().info("{} with ID {} deleted", getDomainClass().getSimpleName(), id);
  }

  @Transactional(readOnly = true)
  @Override
  public Page<D> list(final Pageable pageable)
  {
    getLogger().debug("Getting page {} of {}", pageable, entityClass.getSimpleName());

    Page<E> entities = dao.findAll(Specification.where(null), pageable);

    return entities.map(entity -> dtoMapper.toDto(entity, getDomainClass()));
  }

  @Override
  public Optional<D> findById(final UUID id)
  {
    getLogger().debug("Finding one {} with ID {}", entityClass.getSimpleName(), id);

    Optional<E> entity = dao.findById(id);

    return Optional.ofNullable(dtoMapper.toDto(entity.orElse(null), getDomainClass()));
  }

  protected void validate(final D dto, final CrudOperation operation) throws ValidationException
  {
    if (dtoValidator.isEmpty())
    {
      getLogger().debug("No available validator for {}. Skipping the validation", entityClass.getSimpleName());
      return;
    }

    getLogger().debug("Validating {}: {}", entityClass.getSimpleName(), dto);

    DtoValidator<E, D> validator = dtoValidator.get();
    ValidationResults validationResults = validator.validate(dto);
    validationResults.throwIfError(dto, operation);
  }

  protected abstract Class<D> getDomainClass();
}
