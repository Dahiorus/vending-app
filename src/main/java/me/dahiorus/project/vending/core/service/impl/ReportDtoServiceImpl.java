package me.dahiorus.project.vending.core.service.impl;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.dahiorus.project.vending.common.HasLogger;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.manager.GenericManager;
import me.dahiorus.project.vending.core.manager.impl.ReportManager;
import me.dahiorus.project.vending.core.model.Report;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.model.dto.ReportDTO;
import me.dahiorus.project.vending.core.service.DtoMapper;
import me.dahiorus.project.vending.core.service.ReportDtoService;

@Service
public class ReportDtoServiceImpl extends DtoServiceImpl<Report, ReportDTO, ReportManager>
    implements ReportDtoService, HasLogger
{
  private static final Logger logger = LogManager.getLogger(ReportDtoServiceImpl.class);

  private final GenericManager<VendingMachine> vendingMachineManager;

  public ReportDtoServiceImpl(final ReportManager manager, final DtoMapper dtoMapper,
      final DtoValidator<Report, ReportDTO> dtoValidator, final GenericManager<VendingMachine> vendingMachineManager)
  {
    super(manager, dtoMapper, dtoValidator);
    this.vendingMachineManager = vendingMachineManager;
  }

  @Override
  public Logger getLogger()
  {
    return logger;
  }

  @Transactional(rollbackFor = EntityNotFound.class)
  @Override
  public ReportDTO report(final UUID vendingMachineId) throws EntityNotFound
  {
    logger.traceEntry(() -> vendingMachineId);

    VendingMachine machineToReport = vendingMachineManager.read(vendingMachineId);

    // get the last report to have the last reporting date
    Optional<Report> lastReportOpt = manager.findLastGenerated(machineToReport);
    Instant lastReportingDate = lastReportOpt.map(Report::getCreatedAt)
      .orElse(null);
    Report report = manager.create(new Report(machineToReport, lastReportingDate));
    ReportDTO dto = dtoMapper.toDto(report, getDomainClass());

    logger.info("Report created for vending machine {} : {}", machineToReport.getId(), report);

    return logger.traceExit(dto);
  }

  @Override
  protected Class<ReportDTO> getDomainClass()
  {
    return ReportDTO.class;
  }
}
