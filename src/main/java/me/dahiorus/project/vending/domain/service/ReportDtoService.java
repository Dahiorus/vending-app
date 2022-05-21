package me.dahiorus.project.vending.domain.service;

import java.util.UUID;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.model.Report;
import me.dahiorus.project.vending.domain.model.dto.ReportDTO;
import me.dahiorus.project.vending.domain.model.dto.VendingMachineDTO;

public interface ReportDtoService extends DtoService<Report, ReportDTO>
{
  /**
   * Create a report of a vending machine at this instant
   *
   * @param vendingMachineId The ID of the {@link VendingMachineDTO vending machine}
   * @return The created {@link ReportDTO report} for the machine
   * @throws EntityNotFound if no vending machine match the ID
   */
  ReportDTO report(UUID vendingMachineId) throws EntityNotFound;
}
