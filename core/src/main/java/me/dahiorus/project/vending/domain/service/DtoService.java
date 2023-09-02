package me.dahiorus.project.vending.domain.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.AbstractEntity;
import me.dahiorus.project.vending.domain.model.dto.AbstractDto;

public interface DtoService<E extends AbstractEntity, D extends AbstractDto<E>>
{
  D create(D dto) throws ValidationException;

  void createAll(List<D> dtos);

  D read(UUID id) throws EntityNotFound;

  D update(UUID id, D dto) throws EntityNotFound, ValidationException;

  void delete(UUID id) throws EntityNotFound;

  Page<D> list(Pageable pageable, D criteria, ExampleMatcher exampleMatcher);

  Optional<D> findById(UUID id);
}
