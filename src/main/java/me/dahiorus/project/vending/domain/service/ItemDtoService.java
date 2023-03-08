package me.dahiorus.project.vending.domain.service;

import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.dto.ItemDTO;

public interface ItemDtoService extends DtoService<Item, ItemDTO>, BinaryDataDtoService<ItemDTO>
{
  // marker interface
}
