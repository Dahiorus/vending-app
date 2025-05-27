package me.dahiorus.project.vending.fixture;

import static java.util.UUID.randomUUID;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.entity.ItemName;
import me.dahiorus.project.vending.domain.machine.entity.ClientOrder;
import me.dahiorus.project.vending.domain.machine.entity.ClientOrder.OrderedItem;
import me.dahiorus.project.vending.domain.machine.entity.ClientOrderId;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;

public class ClientOrderFixture {

  public static ClientOrder aClientOrder(
      VendingMachine vendingMachine, String itemName, BigDecimal price, LocalDateTime orderAt) {
    return new ClientOrder(
        new ClientOrderId(randomUUID()),
        vendingMachine,
        new OrderedItem(new ItemId(randomUUID()), ItemName.of(itemName), price),
        orderAt);
  }
}
