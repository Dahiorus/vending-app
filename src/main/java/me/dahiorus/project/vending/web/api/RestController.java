package me.dahiorus.project.vending.web.api;

import java.util.UUID;

import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
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

import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.AbstractEntity;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;
import me.dahiorus.project.vending.web.api.model.ExampleMatcherAdapter;

@RequestMapping(produces = MediaTypes.HAL_JSON_VALUE)
public interface RestController<E extends AbstractEntity, D extends AbstractDTO<E>> extends AppWebService
{
  @Operation(description = "Get a page of entities")
  @GetMapping
  ResponseEntity<PagedModel<EntityModel<D>>> list(@ParameterObject Pageable pageable, @ParameterObject D criteria,
      @ParameterObject ExampleMatcherAdapter exampleMatcherAdapter);

  @Operation(description = "Create a new entity")
  @ApiResponse(responseCode = "201", description = "Entity created")
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<EntityModel<D>> create(@RequestBody D dto) throws ValidationException;

  @Operation(description = "Get an entity by its ID")
  @GetMapping("/{id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}")
  ResponseEntity<EntityModel<D>> read(@PathVariable UUID id) throws EntityNotFound;

  @Operation(description = "Update or create an entity targeted by its ID")
  @ApiResponse(responseCode = "200", description = "Entity updated")
  @PutMapping(value = "/{id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<EntityModel<D>> update(@PathVariable UUID id, @RequestBody D dto) throws ValidationException;

  @Operation(description = "Delete an existing entity targeted by its ID")
  @ApiResponse(responseCode = "204", description = "Entity deleted")
  @DeleteMapping("/{id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}")
  ResponseEntity<Void> delete(@PathVariable UUID id);

  @Operation(description = "Patch an existing entity targeted by its ID")
  @ApiResponse(responseCode = "200", description = "Entity updated")
  @PatchMapping(value = "/{id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}",
      consumes = "application/json-patch+json")
  ResponseEntity<EntityModel<D>> patch(@PathVariable UUID id,
      @ArraySchema(schema = @Schema(implementation = JsonPatchOperation.class,
          description = "JSON patch using RFC 6902")) @RequestBody JsonPatch jsonPatch)
      throws EntityNotFound, ValidationException;
}
