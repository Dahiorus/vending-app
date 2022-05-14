package me.dahiorus.project.vending.util;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.dahiorus.project.vending.core.model.BinaryData;
import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.ItemType;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.core.service.DtoMapper;
import me.dahiorus.project.vending.core.service.impl.DtoMapperImpl;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemBuilder
{
  private final DtoMapper dtoMapper = new DtoMapperImpl();

  private Item item = new Item();

  public static ItemBuilder builder()
  {
    return new ItemBuilder();
  }

  public ItemBuilder name(final String name)
  {
    item.setName(name);
    return this;
  }

  public ItemBuilder id(final UUID id)
  {
    item.setId(id);
    return this;
  }

  public ItemBuilder price(final Double price)
  {
    item.setPrice(price);
    return this;
  }

  public ItemBuilder type(final ItemType type)
  {
    item.setType(type);
    return this;
  }

  public ItemBuilder picture(final String name, final String contentType)
  {
    BinaryData binaryData = new BinaryData();
    binaryData.setName(name);
    binaryData.setContentType(contentType);
    binaryData.setContent(new byte[0]);
    item.setPicture(binaryData);

    return this;
  }

  public Item build()
  {
    return item;
  }

  public ItemDTO buildDto()
  {
    return dtoMapper.toDto(item, ItemDTO.class);
  }
}
