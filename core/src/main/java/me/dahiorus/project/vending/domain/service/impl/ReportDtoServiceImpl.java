package me.dahiorus.project.vending.domain.service.impl;

import static me.dahiorus.project.vending.domain.model.Report.reportAt;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.dao.Dao;
import me.dahiorus.project.vending.domain.dao.ReportDao;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.Report;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.dto.ReportDto;
import me.dahiorus.project.vending.domain.service.DtoMapper;
import me.dahiorus.project.vending.domain.service.ReportDtoService;

@CacheConfig(cacheNames = "reports")
@Log4j2
@Service
public class ReportDtoServiceImpl
  extends DtoServiceImpl<Report, ReportDto, ReportDao>
  implements ReportDtoService
{
  private final Dao<VendingMachine> vendingMachineDao;

  public ReportDtoServiceImpl(final ReportDao dao, final DtoMapper dtoMapper,
    final Dao<VendingMachine> vendingMachineDao)
  {
    super(dao, dtoMapper, null);
    this.vendingMachineDao = vendingMachineDao;
  }

  @Override
  public Logger getLogger()
  {
    return log;
  }

  @CachePut(key = "#result.id")
  @Transactional
  @Override
  public ReportDto report(final UUID vendingMachineId) throws EntityNotFound
  {
    log.traceEntry(() -> vendingMachineId);

    VendingMachine machineToReport = vendingMachineDao.read(vendingMachineId);

    // get the last report to have the last reporting date
    Optional<Report> lastReportOpt = dao.findLastGenerated(machineToReport);
    Instant lastReportingDate = lastReportOpt.map(Report::getCreatedAt)
      .orElse(null);
    Report report = dao.save(reportAt(machineToReport, lastReportingDate));
    ReportDto dto = dtoMapper.toDto(report, getDomainClass());

    log.info("Report created for vending machine {} : {}",
      machineToReport.getId(), report);

    return log.traceExit(dto);
  }

  @Override
  public ReportDto create(final ReportDto dto) throws ValidationException
  {
    throw new UnsupportedOperationException(
      "Cannot directly call create. Call report() instead.");
  }

  @Override
  public ReportDto update(final UUID id, final ReportDto dto)
    throws EntityNotFound, ValidationException
  {
    throw new UnsupportedOperationException(
      "Cannot update a report. Call report() to create a new report of a vending machine.");
  }

  @Override
  protected Class<ReportDto> getDomainClass()
  {
    return ReportDto.class;
  }
}
