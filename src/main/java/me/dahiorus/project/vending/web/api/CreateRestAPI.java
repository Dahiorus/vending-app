package me.dahiorus.project.vending.web.api;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;

public interface CreateRestAPI<D extends AbstractDTO<?>>
{
  @Operation(description = "Create a new entity")
  @ApiResponse(responseCode = "201", description = "Entity created")
  ResponseEntity<EntityModel<D>> create(@RequestBody D dto) throws ValidationException;
}
