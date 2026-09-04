package me.dahiorus.project.vending.domain.stock.port;

import me.dahiorus.project.vending.domain.Findable;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.stock.entity.VendingMachineStock;

public interface VendingMachineStockRepositoryPort
    extends Findable<VendingMachineId, VendingMachineStock> {
  VendingMachineStock update(VendingMachineId id, VendingMachineStock stocksToUpdate);
}
