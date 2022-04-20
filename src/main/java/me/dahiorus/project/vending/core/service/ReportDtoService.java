package me.dahiorus.project.vending.core.service;

import java.util.UUID;

import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.model.Report;
import me.dahiorus.project.vending.core.model.dto.ReportDTO;

public interface ReportDtoService extends DtoService<Report, ReportDTO>
{
  ReportDTO report(UUID vendingMachineId) throws EntityNotFound;
}
