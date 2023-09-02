package me.dahiorus.project.vending.domain.service;

import java.util.Optional;
import java.util.UUID;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.model.dto.AbstractDto;
import me.dahiorus.project.vending.domain.model.dto.BinaryDataDto;

public interface BinaryDataDtoService<D extends AbstractDto<?>>
{
  D uploadImage(UUID id, BinaryDataDto picture) throws EntityNotFound;

  Optional<BinaryDataDto> getImage(UUID id) throws EntityNotFound;
}
