package me.dahiorus.project.vending.domain.stock.port;

import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.stock.entity.Quantity;
import me.dahiorus.project.vending.domain.stock.entity.VendingMachineStock;

public interface VendingMachineStockApiPort {
  VendingMachineStock provision(
      VendingMachineId vendingMachineId, ItemId itemId, Quantity quantity);

  VendingMachineStock get(VendingMachineId vendingMachineId);
}
