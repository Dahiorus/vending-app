package me.dahiorus.project.vending.domain.machine.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.entity.ItemName;

public record ClientOrder(
    ClientOrderId id, VendingMachine vendingMachine, OrderedItem orderedItem, LocalDateTime orderAt)
    implements Serializable {

  public ItemId orderedItemId() {
    return orderedItem.id();
  }

  public ItemName orderedItemName() {
    return orderedItem.name();
  }

  public BigDecimal orderedItemPrice() {
    return orderedItem.price();
  }

  public record OrderedItem(ItemId id, ItemName name, BigDecimal price) implements Serializable {}
}
