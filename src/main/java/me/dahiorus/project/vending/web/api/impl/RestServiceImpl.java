package me.dahiorus.project.vending.web.api.impl;

import static me.dahiorus.project.vending.web.api.JsonPatchHandler.applyPatch;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import com.github.fge.jsonpatch.JsonPatch;

import me.dahiorus.project.vending.common.HasLogger;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.AbstractEntity;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;
import me.dahiorus.project.vending.core.service.DtoService;
import me.dahiorus.project.vending.web.api.RestService;

public abstract class RestServiceImpl<E extends AbstractEntity, D extends AbstractDTO<E>, S extends DtoService<E, D>>
    implements RestService<E, D>, HasLogger
{
  protected final S dtoService;

  protected final RepresentationModelAssembler<D, EntityModel<D>> modelAssembler;

  protected final PagedResourcesAssembler<D> pageModelAssembler;

  protected RestServiceImpl(final S dtoService, final RepresentationModelAssembler<D, EntityModel<D>> modelAssembler,
      final PagedResourcesAssembler<D> pageModelAssembler)
  {
    this.dtoService = dtoService;
    this.modelAssembler = modelAssembler;
    this.pageModelAssembler = pageModelAssembler;
  }

  @Override
  public ResponseEntity<PagedModel<EntityModel<D>>> list(final Pageable pageable)
  {
    getLogger().debug("Getting page {} of entities", pageable);

    Page<D> page = dtoService.list(pageable);

    return ok(pageModelAssembler.toModel(page, modelAssembler));
  }

  @Override
  public ResponseEntity<EntityModel<D>> create(final D dto) throws ValidationException
  {
    getLogger().debug("Creating a new entity: {}", dto);

    D createdEntity = dtoService.create(dto);
    URI location = MvcUriComponentsBuilder.fromController(getClass())
      .path("/{id}")
      .buildAndExpand(createdEntity.getId())
      .toUri();

    return created(location).body(modelAssembler.toModel(createdEntity));
  }

  @Override
  public ResponseEntity<EntityModel<D>> read(final UUID id) throws EntityNotFound
  {
    getLogger().debug("Getting entity with ID {}", id);

    D entity = dtoService.read(id);

    return ok(modelAssembler.toModel(entity));
  }

  @Override
  public ResponseEntity<EntityModel<D>> update(final UUID id, final D dto) throws EntityNotFound, ValidationException
  {
    getLogger().debug("Updating entity with ID {}: {}", id, dto);

    D updatedEntity = dtoService.update(id, dto);

    return ok(modelAssembler.toModel(updatedEntity));
  }

  @Override
  public ResponseEntity<Void> delete(final UUID id)
  {
    getLogger().debug("Deleting entity with ID {}", id);

    try
    {
      dtoService.delete(id);
    }
    catch (EntityNotFound e)
    {
      getLogger().warn("Unable to delete entity with ID {}", id);
    }

    return noContent().build();
  }

  @Override
  public ResponseEntity<EntityModel<D>> patch(final UUID id, final JsonPatch jsonPatch)
      throws EntityNotFound, ValidationException
  {
    getLogger().debug("Patching entity with ID {}: {}", id, jsonPatch);

    D entity = applyPatch(dtoService.read(id), jsonPatch);
    D updatedEntity = dtoService.update(id, entity);

    return ok(modelAssembler.toModel(updatedEntity));
  }
}
