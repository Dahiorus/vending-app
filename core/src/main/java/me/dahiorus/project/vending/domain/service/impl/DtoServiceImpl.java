package me.dahiorus.project.vending.domain.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import me.dahiorus.project.vending.common.HasLogger;
import me.dahiorus.project.vending.domain.dao.Dao;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.AbstractEntity;
import me.dahiorus.project.vending.domain.model.dto.AbstractDto;
import me.dahiorus.project.vending.domain.service.DtoMapper;
import me.dahiorus.project.vending.domain.service.DtoService;
import me.dahiorus.project.vending.domain.service.validation.CrudOperation;
import me.dahiorus.project.vending.domain.service.validation.DtoValidator;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;

@CacheConfig(cacheResolver = "vendingCacheResolver")
public abstract class DtoServiceImpl<E extends AbstractEntity, D extends AbstractDto<E>, R extends Dao<E>>
  implements DtoService<E, D>, HasLogger
{
  protected final R dao;

  protected final DtoMapper dtoMapper;

  protected final Optional<DtoValidator<D>> dtoValidator;

  protected final Class<E> entityClass;

  protected DtoServiceImpl(final R dao, final DtoMapper dtoMapper,
    @Nullable final DtoValidator<D> dtoValidator)
  {
    this.dao = dao;
    this.dtoMapper = dtoMapper;
    this.dtoValidator = Optional.ofNullable(dtoValidator);
    entityClass = dao.getDomainClass();
  }

  @CachePut(key = "#result.id")
  @Transactional
  @Override
  public D create(final D dto) throws ValidationException
  {
    getLogger().debug("Creating a new {}: {}", getDomainClass().getSimpleName(),
      dto);

    validate(dto).throwIfError(dto, CrudOperation.CREATE);

    E entity = dtoMapper.toEntity(dto, entityClass);
    prepareEntity(entity, CrudOperation.CREATE);

    E createdEntity = dao.save(entity);
    D createdDto = dtoMapper.toDto(createdEntity, getDomainClass());

    getLogger().info("{} created: {}", getDomainClass().getSimpleName(),
      createdDto);

    return createdDto;
  }

  @Transactional
  @Override
  public void createAll(final List<D> dtos)
  {
    long createCount = 0;

    for (D dto : dtos)
    {
      try
      {
        create(dto);
      }
      catch (ValidationException e)
      {
        getLogger().error(e.getMessage());
        continue;
      }

      createCount++;
      if (createCount % 200 == 0)
      {
        getLogger().debug("Intermediate flushing after {} creation(s) of {}",
          createCount,
          entityClass.getSimpleName());
        dao.flush();
      }
    }
  }

  @Cacheable(key = "#id")
  @Override
  public D read(final UUID id) throws EntityNotFound
  {
    getLogger().debug("Getting {} with ID {}", getDomainClass().getSimpleName(),
      id);
    E entity = dao.read(id);

    return dtoMapper.toDto(entity, getDomainClass());
  }

  @Caching(evict = @CacheEvict(key = "#id"), put = @CachePut(key = "#result.id"))
  @Transactional
  @Override
  public D update(final UUID id, final D dto)
    throws EntityNotFound, ValidationException
  {
    getLogger().debug("Updating {} with ID {}: {}",
      getDomainClass().getSimpleName(), id, dto);

    E entity = dao.read(id);

    dto.setId(id);
    validate(dto).throwIfError(dto, CrudOperation.UPDATE);
    dtoMapper.patchEntity(dto, entity);
    prepareEntity(entity, CrudOperation.UPDATE);

    E updatedEntity = dao.save(entity);
    D updatedDto = dtoMapper.toDto(updatedEntity, getDomainClass());

    getLogger().info("{} updated: {}", getDomainClass().getSimpleName(),
      updatedDto);

    return updatedDto;
  }

  @CacheEvict(key = "#id")
  @Transactional
  @Override
  public void delete(final UUID id) throws EntityNotFound
  {
    getLogger().debug("Deleting {} with ID {}",
      getDomainClass().getSimpleName(), id);

    E entity = dao.read(id);
    prepareEntity(entity, CrudOperation.DELETE);
    dao.delete(entity);

    getLogger().info("{} deleted: ID {}", getDomainClass().getSimpleName(), id);
  }

  // @Cacheable
  @Override
  public Page<D> list(@Nonnull final Pageable pageable,
    @Nullable final D criteria,
    @Nullable final ExampleMatcher exampleMatcher)
  {
    Page<E> entities;

    if (criteria != null)
    {
      getLogger().debug("Getting page {} of {} matching criteria {}", pageable,
        getDomainClass().getSimpleName(), criteria);
      Example<E> example = toExample(criteria, exampleMatcher);
      entities = dao.findAll(example, pageable);
    }
    else
    {
      getLogger().debug("Getting page {} of {}", pageable,
        getDomainClass().getSimpleName());
      entities = dao.findAll(pageable);
    }

    return entities.map(entity -> dtoMapper.toDto(entity, getDomainClass()));
  }

  private Example<E> toExample(@Nonnull final D criteria,
    @Nullable final ExampleMatcher exampleMatcher)
  {
    E probe = dtoMapper.toEntity(criteria, entityClass);

    return exampleMatcher == null ? Example.of(probe)
      : Example.of(probe, exampleMatcher);
  }

  @Cacheable(key = "#id", unless = "#result == null")
  @Override
  public Optional<D> findById(final UUID id)
  {
    getLogger().debug("Finding one {} with ID {}",
      getDomainClass().getSimpleName(), id);

    Optional<E> entity = dao.findById(id);

    return Optional
      .ofNullable(dtoMapper.toDto(entity.orElse(null), getDomainClass()));
  }

  @Nonnull
  protected ValidationResults validate(final D dto)
  {
    if (dtoValidator.isEmpty())
    {
      getLogger().debug(
        "No available validator for {}. Skipping the validation",
        getDomainClass().getSimpleName());
      return new ValidationResults();
    }

    getLogger().debug("Validating {}: {}", getDomainClass().getSimpleName(),
      dto);

    return dtoValidator.get()
      .validate(dto);
  }

  protected void prepareEntity(final E entity, final CrudOperation operation)
  {
    // override this method to modify the entity before calling the DAO
  }

  protected abstract Class<D> getDomainClass();
}
