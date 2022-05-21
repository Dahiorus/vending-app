package me.dahiorus.project.vending.domain.service;

import java.util.Optional;
import java.util.UUID;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.dto.BinaryDataDTO;
import me.dahiorus.project.vending.domain.model.dto.ItemDTO;

public interface ItemDtoService extends DtoService<Item, ItemDTO>
{
  ItemDTO uploadImage(UUID id, BinaryDataDTO picture) throws EntityNotFound;

  Optional<BinaryDataDTO> getImage(UUID id) throws EntityNotFound;
}
