package me.dahiorus.project.vending.web.api;

import java.util.UUID;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;

public interface UpdateRestAPI<D extends AbstractDTO<?>>
{
  @Operation(description = "Update or create an entity targeted by its ID")
  @ApiResponse(responseCode = "200", description = "Entity updated")
  ResponseEntity<EntityModel<D>> update(@PathVariable UUID id, @RequestBody D dto) throws ValidationException;
}
