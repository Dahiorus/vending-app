package me.dahiorus.project.vending.web.api.impl;

import static me.dahiorus.project.vending.web.api.request.JsonPatchHandler.applyPatch;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import com.github.fge.jsonpatch.JsonPatch;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import me.dahiorus.project.vending.common.HasLogger;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;
import me.dahiorus.project.vending.core.service.DtoService;
import me.dahiorus.project.vending.web.api.RestController;
import me.dahiorus.project.vending.web.api.request.ExampleMatcherAdapter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@RequestMapping(produces = MediaTypes.HAL_JSON_VALUE)
public abstract class RestControllerImpl<D extends AbstractDTO<?>, S extends DtoService<?, D>>
    implements RestController<D>, HasLogger
{
  protected final S dtoService;

  protected final RepresentationModelAssembler<D, EntityModel<D>> modelAssembler;

  protected final PagedResourcesAssembler<D> pageModelAssembler;

  @GetMapping
  @Override
  public ResponseEntity<PagedModel<EntityModel<D>>> list(final Pageable pageable, final D criteria,
      final ExampleMatcherAdapter exampleMatcherAdapter)
  {
    getLogger().debug("Getting page {} of entities matching criteria [{}, matcher: {}]", pageable, criteria,
        exampleMatcherAdapter);

    Page<D> page = dtoService.list(pageable, criteria, exampleMatcherAdapter.toExampleMatcher());

    return ok(pageModelAssembler.toModel(page, modelAssembler));
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
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

  @GetMapping("/{id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}")
  @Override
  public ResponseEntity<EntityModel<D>> read(final UUID id) throws EntityNotFound
  {
    getLogger().debug("Getting entity with ID {}", id);

    D entity = dtoService.read(id);

    return ok(modelAssembler.toModel(entity));
  }

  @PutMapping(value = "/{id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @Override
  public ResponseEntity<EntityModel<D>> update(final UUID id, final D dto) throws ValidationException
  {

    D updatedEntity;
    try
    {
      getLogger().debug("Updating entity with ID {}: {}", id, dto);
      updatedEntity = dtoService.update(id, dto);
    }
    catch (EntityNotFound ignore)
    {
      getLogger().debug("Creating entity with ID {}: {}", id, dto);
      dto.setId(id);
      updatedEntity = dtoService.create(dto);
    }

    return ok(modelAssembler.toModel(updatedEntity));
  }

  @DeleteMapping("/{id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}")
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

  @PatchMapping(value = "/{id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}",
      consumes = "application/json-patch+json")
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
