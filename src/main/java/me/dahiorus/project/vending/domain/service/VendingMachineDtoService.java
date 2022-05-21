package me.dahiorus.project.vending.domain.service;

import java.util.UUID;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.dto.VendingMachineDTO;

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
