package me.dahiorus.project.vending.domain.exception;

import static java.lang.String.format;

import me.dahiorus.project.vending.domain.item.entity.ItemId;

public class ItemStillInStock extends RuntimeException {
  public ItemStillInStock(ItemId itemId) {
    super(format("Item %s cannot be deleted: it is still in stock in a vending machine", itemId));
  }
}
