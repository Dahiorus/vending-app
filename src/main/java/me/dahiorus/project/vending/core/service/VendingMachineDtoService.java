package me.dahiorus.project.vending.core.service;

import java.util.UUID;

import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.model.dto.VendingMachineDTO;

public interface VendingMachineDtoService extends DtoService<VendingMachine, VendingMachineDTO>
{
  /**
   * Reset all a vending machine status to OK. If the power status is OFF, the machine is restarted.
   *
   * @param id The vending machine ID
   * @return The reset machine
   * @throws EntityNotFound if no vending machine match the ID
   */
  VendingMachineDTO resetStatus(UUID id) throws EntityNotFound;
}
