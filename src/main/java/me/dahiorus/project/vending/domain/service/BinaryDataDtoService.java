package me.dahiorus.project.vending.domain.service;

import java.util.Optional;
import java.util.UUID;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.model.dto.AbstractDTO;
import me.dahiorus.project.vending.domain.model.dto.BinaryDataDTO;

public interface BinaryDataDtoService<D extends AbstractDTO<?>>
{
  D uploadImage(UUID id, BinaryDataDTO picture) throws EntityNotFound;

  Optional<BinaryDataDTO> getImage(UUID id) throws EntityNotFound;
}
