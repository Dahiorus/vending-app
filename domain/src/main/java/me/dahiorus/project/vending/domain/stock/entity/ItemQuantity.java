package me.dahiorus.project.vending.domain.stock.entity;

import static me.dahiorus.project.vending.domain.stock.entity.Quantity.empty;

import me.dahiorus.project.vending.domain.item.entity.Item;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.entity.ItemName;

public record ItemQuantity(Item item, Quantity quantity) {

  public ItemQuantity {
    if (item == null || quantity == null) {
      throw new IllegalArgumentException("Item and quantity must not be null");
    }
  }

  public boolean isEmpty() {
    return empty().equals(quantity);
  }

  public ItemId itemId() {
    return item.id();
  }

  public ItemName itemName() {
    return item.name();
  }

  public Integer quantityValue() {
    return quantity.value();
  }

  public boolean doesNotHaveStock() {
    return quantity.value() <= 0;
  }

  public ItemQuantity decrementQuantity() {
    if (doesNotHaveStock()) {
      throw new IllegalStateException("Cannot decrement quantity below zero");
    }
    return new ItemQuantity(item, quantity.decrement());
  }
}
