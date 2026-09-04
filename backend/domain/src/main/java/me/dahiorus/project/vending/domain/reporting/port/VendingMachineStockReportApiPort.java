package me.dahiorus.project.vending.domain.reporting.port;

import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStockReport;

public interface VendingMachineStockReportApiPort {
  VendingMachineStockReport reportStock(VendingMachineId vendingMachineId) throws ResourceNotFound;
}
