package me.dahiorus.project.vending.domain.reporting.usecase;

import me.dahiorus.project.vending.domain.documentation.DomainService;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.port.VendingMachineRepositoryPort;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStockReport;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStockReportToCreate;
import me.dahiorus.project.vending.domain.reporting.port.VendingMachineStockReportRepositoryPort;
import me.dahiorus.project.vending.domain.stock.port.VendingMachineStockRepositoryPort;

@DomainService
public class ReportVendingMachineStock {

  private final VendingMachineRepositoryPort vendingMachineRepository;
  private final VendingMachineStockRepositoryPort vendingMachineStockRepository;
  private final VendingMachineStockReportRepositoryPort vendingMachineStockReportRepository;

  public ReportVendingMachineStock(
      final VendingMachineRepositoryPort vendingMachineRepository,
      final VendingMachineStockRepositoryPort vendingMachineStockRepository,
      final VendingMachineStockReportRepositoryPort vendingMachineStockReportRepository) {
    this.vendingMachineRepository = vendingMachineRepository;
    this.vendingMachineStockRepository = vendingMachineStockRepository;
    this.vendingMachineStockReportRepository = vendingMachineStockReportRepository;
  }

  public VendingMachineStockReport execute(final VendingMachineId vendingMachineId) {
    var vendingMachine =
        vendingMachineRepository
            .find(vendingMachineId)
            .orElseThrow(() -> new ResourceNotFound(vendingMachineId));
    var stockToReport =
        vendingMachineStockRepository
            .find(vendingMachineId)
            .orElseThrow(() -> new ResourceNotFound(vendingMachineId));

    return vendingMachineStockReportRepository.create(
        new VendingMachineStockReportToCreate(vendingMachine.serialNumber(), stockToReport));
  }
}
