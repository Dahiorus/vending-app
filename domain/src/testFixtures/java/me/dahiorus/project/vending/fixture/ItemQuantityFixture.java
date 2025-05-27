package me.dahiorus.project.vending.fixture;

import me.dahiorus.project.vending.domain.item.entity.Item;
import me.dahiorus.project.vending.domain.stock.entity.ItemQuantity;
import me.dahiorus.project.vending.domain.stock.entity.Quantity;

public class ItemQuantityFixture {
  public static ItemQuantity itemQuantity(Item item, int quantity) {
    return new ItemQuantity(item, new Quantity(quantity));
  }
}
