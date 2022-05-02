package me.dahiorus.project.vending.core.service.impl;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.dao.ReportDAO;
import me.dahiorus.project.vending.core.dao.impl.AbstractDAO;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.Report;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.model.dto.ReportDTO;
import me.dahiorus.project.vending.core.service.DtoMapper;
import me.dahiorus.project.vending.core.service.ReportDtoService;

@Log4j2
@Service
public class ReportDtoServiceImpl extends DtoServiceImpl<Report, ReportDTO, ReportDAO>
    implements ReportDtoService
{
  private final AbstractDAO<VendingMachine> vendingMachineDao;

  public ReportDtoServiceImpl(final ReportDAO manager, final DtoMapper dtoMapper,
      final AbstractDAO<VendingMachine> vendingMachineDao)
  {
    super(manager, dtoMapper, null);
    this.vendingMachineDao = vendingMachineDao;
  }

  @Override
  public Logger getLogger()
  {
    return log;
  }

  @Transactional(rollbackFor = EntityNotFound.class)
  @Override
  public ReportDTO report(final UUID vendingMachineId) throws EntityNotFound
  {
    log.traceEntry(() -> vendingMachineId);

    VendingMachine machineToReport = vendingMachineDao.read(vendingMachineId);

    // get the last report to have the last reporting date
    Optional<Report> lastReportOpt = dao.findLastGenerated(machineToReport);
    Instant lastReportingDate = lastReportOpt.map(Report::getCreatedAt)
      .orElse(null);
    Report report = dao.save(Report.of(machineToReport, lastReportingDate));
    ReportDTO dto = dtoMapper.toDto(report, getDomainClass());

    log.info("Report created for vending machine {} : {}", machineToReport.getId(), report);

    return log.traceExit(dto);
  }

  @Override
  public ReportDTO create(final ReportDTO dto) throws ValidationException
  {
    throw new UnsupportedOperationException("Cannot directly call create. Call report() instead.");
  }

  @Override
  public ReportDTO update(final UUID id, final ReportDTO dto) throws EntityNotFound, ValidationException
  {
    throw new UnsupportedOperationException(
        "Cannot update a report. Call report() to create a new report of a vending machine.");
  }

  @Override
  protected Class<ReportDTO> getDomainClass()
  {
    return ReportDTO.class;
  }
}
