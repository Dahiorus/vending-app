package me.dahiorus.project.vending.domain.reporting.usecase;

import me.dahiorus.project.vending.domain.documentation.DomainService;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.port.ClientOrderRepositoryPort;
import me.dahiorus.project.vending.domain.machine.port.VendingMachineRepositoryPort;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReport;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReportToCreate;
import me.dahiorus.project.vending.domain.reporting.port.VendingMachineClientOrdersReportRepositoryPort;

@DomainService
public class ReportVendingMachineClientOrders {

  private final VendingMachineRepositoryPort vendingMachineRepository;
  private final ClientOrderRepositoryPort clientOrderRepository;
  private final VendingMachineClientOrdersReportRepositoryPort
      vendingMachineClientOrdersReportRepository;

  public ReportVendingMachineClientOrders(
      final VendingMachineRepositoryPort vendingMachineRepository,
      final ClientOrderRepositoryPort clientOrderRepository,
      final VendingMachineClientOrdersReportRepositoryPort
          vendingMachineClientOrdersReportRepository) {
    this.vendingMachineRepository = vendingMachineRepository;
    this.clientOrderRepository = clientOrderRepository;
    this.vendingMachineClientOrdersReportRepository = vendingMachineClientOrdersReportRepository;
  }

  public VendingMachineClientOrdersReport execute(VendingMachineId vendingMachineId) {
    var vendingMachine =
        vendingMachineRepository
            .find(vendingMachineId)
            .orElseThrow(() -> new ResourceNotFound(vendingMachineId));
    var clientOrders =
        vendingMachineClientOrdersReportRepository
            .findLastGeneratedOf(vendingMachine)
            .map(VendingMachineClientOrdersReport::reportedAt)
            .map(
                reportedAt ->
                    clientOrderRepository.findAllOfVendingMachineSince(
                        vendingMachineId, reportedAt))
            .orElseGet(() -> clientOrderRepository.findAllOfVendingMachine(vendingMachineId));

    return vendingMachineClientOrdersReportRepository.create(
        new VendingMachineClientOrdersReportToCreate(vendingMachine, clientOrders));
  }
}
