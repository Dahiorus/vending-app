package me.dahiorus.project.vending.domain.machine.port;

import me.dahiorus.project.vending.domain.exception.ItemStockIsEmpty;
import me.dahiorus.project.vending.domain.exception.NotWorkingVendingMachine;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.machine.entity.ClientOrder;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;

public interface OrderItemApiPort {
  ClientOrder orderItem(VendingMachineId vendingMachineId, ItemId itemId)
      throws ResourceNotFound, NotWorkingVendingMachine, ItemStockIsEmpty;
}
