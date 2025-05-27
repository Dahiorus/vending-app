package me.dahiorus.project.vending.domain.reporting.entity;

import java.util.List;
import me.dahiorus.project.vending.domain.machine.entity.ClientOrder;
import me.dahiorus.project.vending.domain.machine.entity.SerialNumber;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;

public record VendingMachineClientOrdersReportToCreate(
    VendingMachine vendingMachine, List<ClientOrder> clientOrders) {

  public SerialNumber vendingMachineSerialNumber() {
    return vendingMachine.serialNumber();
  }
}
