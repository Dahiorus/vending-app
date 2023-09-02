package me.dahiorus.project.vending.web.api;

import java.util.UUID;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;

import com.github.fge.jsonpatch.JsonPatch;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.dto.AbstractDto;

public interface PatchRestApi<D extends AbstractDto<?>>
{
  ResponseEntity<EntityModel<D>> patch(UUID id, JsonPatch jsonPatch) throws EntityNotFound, ValidationException;
}
