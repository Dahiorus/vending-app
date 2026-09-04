package me.dahiorus.project.vending.domain.reporting.port;

import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReport;

public interface VendingMachineClientOrdersReportApiPort {
  VendingMachineClientOrdersReport reportClientOrders(VendingMachineId vendingMachineId)
      throws ResourceNotFound;
}
