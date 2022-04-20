package me.dahiorus.project.vending.core.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.InvalidData;
import me.dahiorus.project.vending.core.model.AbstractEntity;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;

public interface DtoService<E extends AbstractEntity, D extends AbstractDTO<E>>
{
  D create(D dto) throws InvalidData;

  D read(UUID id) throws EntityNotFound;

  D update(UUID id, D dto) throws EntityNotFound, InvalidData;

  void delete(UUID id) throws EntityNotFound;

  Page<D> list(Pageable pageable);

  Optional<D> findById(UUID id);
}
