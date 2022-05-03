package me.dahiorus.project.vending.core.service;

import java.util.UUID;

import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.dto.BinaryDataDTO;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;

public interface ItemDtoService extends DtoService<Item, ItemDTO>
{
  ItemDTO uploadPicture(UUID id, BinaryDataDTO picture) throws EntityNotFound;

  BinaryDataDTO getPicture(UUID id) throws EntityNotFound;
}
