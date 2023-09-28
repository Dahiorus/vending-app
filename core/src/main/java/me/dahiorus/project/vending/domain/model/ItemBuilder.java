package me.dahiorus.project.vending.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.dahiorus.project.vending.domain.model.dto.ItemDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemBuilder
{
  private Item item = new Item();
  private ItemDto dto = new ItemDto();

  public static ItemBuilder builder()
  {
    return new ItemBuilder();
  }

  public ItemBuilder name(final String name)
  {
    item.setName(name);
    dto.setName(name);
    return this;
  }

  public ItemBuilder id(final UUID id)
  {
    item.setId(id);
    dto.setId(id);
    return this;
  }

  public ItemBuilder price(final BigDecimal price)
  {
    item.setPrice(price);
    dto.setPrice(price);
    return this;
  }

  public ItemBuilder type(final ItemType type)
  {
    item.setType(type);
    dto.setType(type);
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

  public ItemDto buildDto()
  {
    return dto;
  }
}
