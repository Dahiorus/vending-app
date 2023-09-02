package me.dahiorus.project.vending.domain.service;

import java.util.UUID;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.model.Report;
import me.dahiorus.project.vending.domain.model.dto.ReportDto;
import me.dahiorus.project.vending.domain.model.dto.VendingMachineDto;

public interface ReportDtoService extends DtoService<Report, ReportDto>
{
  /**
   * Create a report of a vending machine at this instant
   *
   * @param vendingMachineId The ID of the {@link VendingMachineDto vending machine}
   * @return The created {@link ReportDto report} for the machine
   * @throws EntityNotFound if no vending machine match the ID
   */
  ReportDto report(UUID vendingMachineId) throws EntityNotFound;
}
