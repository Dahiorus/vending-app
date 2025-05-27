package me.dahiorus.project.vending.domain.item.entity;

import java.io.Serializable;
import java.math.BigDecimal;

public record Item(ItemId id, ItemName name, BigDecimal price, ItemType type)
    implements Serializable {
  public Item updateFrom(ItemToUpdate toUpdate) {
    return new Item(id, name, toUpdate.price(), type);
  }
}
