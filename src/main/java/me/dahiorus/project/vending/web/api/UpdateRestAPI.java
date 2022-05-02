package me.dahiorus.project.vending.web.api;

import java.util.UUID;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;

import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;

public interface UpdateRestAPI<D extends AbstractDTO<?>>
{
  ResponseEntity<EntityModel<D>> update(UUID id, D dto) throws ValidationException;
}
