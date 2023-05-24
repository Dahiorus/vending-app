package me.dahiorus.project.vending.web.api;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;

import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.dto.AbstractDTO;

public interface CreateRestAPI<D extends AbstractDTO<?>>
{
  ResponseEntity<EntityModel<D>> create(D dto) throws ValidationException;
}
