package me.dahiorus.project.vending.domain.exception;

import static java.lang.String.format;

import me.dahiorus.project.vending.domain.item.entity.Item;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;

public class ItemStockIsEmpty extends RuntimeException {
  public ItemStockIsEmpty(Item itemToOrder, VendingMachine vendingMachine) {
    super(
        format(
            "Item %s is out of stock in vending machine %s",
            itemToOrder.id(), vendingMachine.id()));
  }
}
