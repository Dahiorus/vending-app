package me.dahiorus.project.vending.core.service.impl;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import me.dahiorus.project.vending.common.HasLogger;
import me.dahiorus.project.vending.core.dao.DAO;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.AbstractEntity;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;
import me.dahiorus.project.vending.core.service.DtoMapper;
import me.dahiorus.project.vending.core.service.DtoService;
import me.dahiorus.project.vending.core.service.validation.CrudOperation;
import me.dahiorus.project.vending.core.service.validation.DtoValidator;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;

public abstract class DtoServiceImpl<E extends AbstractEntity, D extends AbstractDTO<E>, R extends DAO<E>>
    implements DtoService<E, D>, HasLogger
{
  protected final R dao;

  protected final DtoMapper dtoMapper;

  protected final Optional<DtoValidator<D>> dtoValidator;

  protected final Class<E> entityClass;

  protected DtoServiceImpl(final R dao, final DtoMapper dtoMapper, @Nullable final DtoValidator<D> dtoValidator)
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
    getLogger().debug("Creating a new {}: {}", getDomainClass().getSimpleName(), dto);

    validate(dto, CrudOperation.CREATE);

    E entity = dtoMapper.toEntity(dto, entityClass);
    doBeforeCallingDao(entity, CrudOperation.CREATE);

    E createdEntity = dao.save(entity);
    D createdDto = dtoMapper.toDto(createdEntity, getDomainClass());

    getLogger().info("{} created: {}", getDomainClass().getSimpleName(), createdDto);

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
    getLogger().debug("Updating {} with ID {}: {}", getDomainClass().getSimpleName(), id, dto);

    E entity = dao.read(id);

    dto.setId(id);
    validate(dto, CrudOperation.UPDATE);
    dtoMapper.patchEntity(dto, entity);
    doBeforeCallingDao(entity, CrudOperation.UPDATE);

    E updatedEntity = dao.save(entity);
    D updatedDto = dtoMapper.toDto(updatedEntity, getDomainClass());

    getLogger().info("{} updated: {}", getDomainClass().getSimpleName(), updatedDto);

    return updatedDto;
  }

  @Transactional(rollbackFor = EntityNotFound.class)
  @Override
  public void delete(final UUID id) throws EntityNotFound
  {
    getLogger().debug("Deleting {} with ID {}", getDomainClass().getSimpleName(), id);

    E entity = dao.read(id);
    doBeforeCallingDao(entity, CrudOperation.DELETE);
    dao.delete(entity);

    getLogger().info("{} deleted: ID {}", getDomainClass().getSimpleName(), id);
  }

  @Transactional(readOnly = true)
  @Override
  public Page<D> list(@Nonnull final Pageable pageable, @Nullable final D criteria,
      @Nullable final ExampleMatcher exampleMatcher)
  {
    Page<E> entities;

    if (criteria != null)
    {
      getLogger().debug("Getting page {} of {} matching criteria {}", pageable, getDomainClass().getSimpleName(),
          criteria);
      Example<E> example = toExample(criteria, exampleMatcher);
      entities = dao.findAll(example, pageable);
    }
    else
    {
      getLogger().debug("Getting page {} of {}", pageable, getDomainClass().getSimpleName());
      entities = dao.findAll(pageable);
    }

    return entities.map(entity -> dtoMapper.toDto(entity, getDomainClass()));
  }

  private Example<E> toExample(@Nonnull final D criteria, @Nullable final ExampleMatcher exampleMatcher)
  {
    E probe = dtoMapper.toEntity(criteria, entityClass);

    return exampleMatcher == null ? Example.of(probe) : Example.of(probe, exampleMatcher);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<D> findById(final UUID id)
  {
    getLogger().debug("Finding one {} with ID {}", getDomainClass().getSimpleName(), id);

    Optional<E> entity = dao.findById(id);

    return Optional.ofNullable(dtoMapper.toDto(entity.orElse(null), getDomainClass()));
  }

  protected void validate(final D dto, final CrudOperation operation) throws ValidationException
  {
    if (dtoValidator.isEmpty())
    {
      getLogger().debug("No available validator for {}. Skipping the validation", getDomainClass().getSimpleName());
      return;
    }

    getLogger().debug("Validating {}: {}", getDomainClass().getSimpleName(), dto);

    ValidationResults validationResults = dtoValidator.get()
      .validate(dto);
    doExtraValidation(dto, validationResults);
    validationResults.throwIfError(dto, operation);
  }

  protected void doExtraValidation(final D dto, final ValidationResults validationResults)
  {
    // override this method to add specific validation
  }

  protected void doBeforeCallingDao(final E entity, final CrudOperation operation)
  {
    // override this method to modify the entity before calling the DAO
  }

  protected abstract Class<D> getDomainClass();
}
