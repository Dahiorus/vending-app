package me.dahiorus.project.vending.domain.reporting.port;

import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStatusReport;

public interface VendingMachineStatusApiPort {
  VendingMachine resetStatus(VendingMachineId id) throws ResourceNotFound;

  VendingMachineStatusReport reportStatus(VendingMachineId id) throws ResourceNotFound;
}
