package me.dahiorus.project.vending.infrastructure.rest.entity.item;

import java.math.BigDecimal;
import me.dahiorus.project.vending.domain.item.entity.ItemName;
import me.dahiorus.project.vending.domain.item.entity.ItemToCreate;
import me.dahiorus.project.vending.domain.item.entity.ItemType;

public record ItemToCreateDto(String name, ItemType type, BigDecimal price) {
  public ItemToCreate toDomain() {
    return new ItemToCreate(ItemName.of(name), type, price);
  }
}
