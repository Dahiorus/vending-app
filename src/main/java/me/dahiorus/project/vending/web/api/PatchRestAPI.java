package me.dahiorus.project.vending.web.api;

import java.util.UUID;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;

public interface PatchRestAPI<D extends AbstractDTO<?>>
{
  @Operation(description = "Patch an existing entity targeted by its ID")
  @ApiResponse(responseCode = "200", description = "Entity updated")
  ResponseEntity<EntityModel<D>> patch(@PathVariable UUID id,
      @ArraySchema(schema = @Schema(implementation = JsonPatchOperation.class,
          description = "JSON patch using RFC 6902")) @RequestBody JsonPatch jsonPatch)
      throws EntityNotFound, ValidationException;
}
