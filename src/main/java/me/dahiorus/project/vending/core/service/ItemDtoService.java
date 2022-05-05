package me.dahiorus.project.vending.core.service;

import java.util.Optional;
import java.util.UUID;

import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.dto.BinaryDataDTO;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;

public interface ItemDtoService extends DtoService<Item, ItemDTO>
{
  ItemDTO uploadImage(UUID id, BinaryDataDTO picture) throws EntityNotFound;

  Optional<BinaryDataDTO> getImage(UUID id) throws EntityNotFound;
}
