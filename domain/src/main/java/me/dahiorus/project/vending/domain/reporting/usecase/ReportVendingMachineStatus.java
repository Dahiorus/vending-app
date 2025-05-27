package me.dahiorus.project.vending.domain.reporting.usecase;

import me.dahiorus.project.vending.domain.documentation.DomainService;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.port.VendingMachineRepositoryPort;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStatusReport;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStatusReportToCreate;
import me.dahiorus.project.vending.domain.reporting.port.VendingMachineStatusReportRepositoryPort;

@DomainService
public class ReportVendingMachineStatus {

  private final VendingMachineRepositoryPort vendingMachineRepository;
  private final VendingMachineStatusReportRepositoryPort vendingMachineReportRepository;

  public ReportVendingMachineStatus(
      VendingMachineRepositoryPort vendingMachineRepository,
      VendingMachineStatusReportRepositoryPort vendingMachineReportRepository) {
    this.vendingMachineRepository = vendingMachineRepository;
    this.vendingMachineReportRepository = vendingMachineReportRepository;
  }

  public VendingMachineStatusReport execute(VendingMachineId id) {
    var vendingMachine =
        vendingMachineRepository.find(id).orElseThrow(() -> new ResourceNotFound(id));

    var statusReport =
        new VendingMachineStatusReportToCreate(
            vendingMachine.serialNumber(),
            vendingMachine.lastIntervention(),
            vendingMachine.status());

    return vendingMachineReportRepository.create(statusReport);
  }
}
