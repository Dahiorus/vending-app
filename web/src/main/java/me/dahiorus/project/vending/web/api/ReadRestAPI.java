package me.dahiorus.project.vending.web.api;

import java.util.UUID;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.model.dto.AbstractDTO;

public interface ReadRestAPI<D extends AbstractDTO<?>>
{
  ResponseEntity<EntityModel<D>> read(UUID id) throws EntityNotFound;
}
