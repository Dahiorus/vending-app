package me.dahiorus.project.vending.web.api.impl;

import static me.dahiorus.project.vending.web.api.request.JsonPatchHandler.applyPatch;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

import java.net.URI;
import java.util.UUID;

import org.springdoc.api.annotations.ParameterObject;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.dahiorus.project.vending.common.HasLogger;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;
import me.dahiorus.project.vending.core.service.DtoService;
import me.dahiorus.project.vending.web.api.RestController;
import me.dahiorus.project.vending.web.api.request.ExampleMatcherAdapter;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@RequestMapping(produces = MediaTypes.HAL_JSON_VALUE)
public abstract class RestControllerImpl<D extends AbstractDTO<?>, S extends DtoService<?, D>>
    implements RestController<D>, HasLogger
{
  protected final S dtoService;

  protected final RepresentationModelAssembler<D, EntityModel<D>> modelAssembler;

  protected final PagedResourcesAssembler<D> pageModelAssembler;

  @Operation(description = "Get a page of entities")
  @ApiResponse(responseCode = "200", description = "Entities found")
  @GetMapping
  @Override
  public ResponseEntity<PagedModel<EntityModel<D>>> list(@ParameterObject final Pageable pageable,
      @ParameterObject final D criteria,
      @ParameterObject final ExampleMatcherAdapter exampleMatcherAdapter)
  {
    getLogger().debug("Getting page {} of entities matching criteria [{}, matcher: {}]", pageable, criteria,
        exampleMatcherAdapter);

    Page<D> page = dtoService.list(pageable, criteria, exampleMatcherAdapter.toExampleMatcher());

    return ok(pageModelAssembler.toModel(page, modelAssembler));
  }

  @Operation(description = "Create a new entity")
  @ApiResponse(responseCode = "201", description = "Entity created")
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @Override
  public ResponseEntity<EntityModel<D>> create(@RequestBody final D dto) throws ValidationException
  {
    getLogger().debug("Creating a new entity: {}", dto);

    D createdEntity = dtoService.create(dto);
    URI location = MvcUriComponentsBuilder.fromMethodName(getClass(), "read", createdEntity.getId())
      .build()
      .toUri();

    getLogger().info("Created entity: {}", location);

    return created(location).body(modelAssembler.toModel(createdEntity));
  }

  @Operation(description = "Get an entity by its ID")
  @ApiResponse(responseCode = "200", description = "Entity found")
  @GetMapping("/{id}")
  @Override
  public ResponseEntity<EntityModel<D>> read(@PathVariable final UUID id) throws EntityNotFound
  {
    getLogger().debug("Getting entity with ID {}", id);

    D entity = dtoService.read(id);

    return ok(modelAssembler.toModel(entity));
  }

  @Operation(description = "Update or create an entity targeted by its ID")
  @ApiResponse(responseCode = "200", description = "Entity created or updated")
  @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Override
  public ResponseEntity<EntityModel<D>> update(@PathVariable final UUID id, @RequestBody final D dto)
      throws ValidationException
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

    getLogger().info("Updated entity: {}", updatedEntity);

    return ok(modelAssembler.toModel(updatedEntity));
  }

  @Operation(description = "Delete an existing entity targeted by its ID")
  @ApiResponse(responseCode = "204", description = "Entity deleted")
  @DeleteMapping("/{id}")
  @Override
  public ResponseEntity<Void> delete(@PathVariable final UUID id)
  {
    getLogger().debug("Deleting entity with ID {}", id);

    try
    {
      dtoService.delete(id);
      getLogger().info("Deleted entity: {}", id);
    }
    catch (EntityNotFound e)
    {
      getLogger().warn("Unable to delete entity with ID {}", id);
    }

    return noContent().build();
  }

  @Operation(description = "Patch an existing entity targeted by its ID")
  @ApiResponse(responseCode = "200", description = "Entity updated")
  @PatchMapping(value = "/{id}", consumes = "application/json-patch+json")
  @Override
  public ResponseEntity<EntityModel<D>> patch(@PathVariable final UUID id,
      @ArraySchema(schema = @Schema(implementation = JsonPatchOperation.class,
          description = "JSON patch using RFC 6902")) @RequestBody final JsonPatch jsonPatch)
      throws EntityNotFound, ValidationException
  {
    getLogger().debug("Patching entity with ID {}: {}", id, jsonPatch);

    D entity = applyPatch(dtoService.read(id), jsonPatch);
    D updatedEntity = dtoService.update(id, entity);

    getLogger().info("Patched entity: {}", updatedEntity);

    return ok(modelAssembler.toModel(updatedEntity));
  }
}
