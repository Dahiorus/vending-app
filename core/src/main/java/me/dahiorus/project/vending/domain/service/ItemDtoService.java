package me.dahiorus.project.vending.domain.service;

import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.dto.ItemDto;

public interface ItemDtoService extends DtoService<Item, ItemDto>, BinaryDataDtoService<ItemDto>
{
  // marker interface
}
