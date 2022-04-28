package me.dahiorus.project.vending.web.api;

import java.util.UUID;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;

public interface ReadRestAPI<D extends AbstractDTO<?>>
{
  @Operation(description = "Get an entity by its ID")
  ResponseEntity<EntityModel<D>> read(@PathVariable UUID id) throws EntityNotFound;
}
