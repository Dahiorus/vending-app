package me.dahiorus.project.vending.infrastructure.rest.entity.stock;

import java.util.UUID;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.stock.entity.Quantity;

public record ItemToProvisionDto(UUID itemId, int quantity) {
  public ItemId toItemId() {
    return new ItemId(itemId);
  }

  public Quantity toQuantity() {
    return new Quantity(quantity);
  }
}
