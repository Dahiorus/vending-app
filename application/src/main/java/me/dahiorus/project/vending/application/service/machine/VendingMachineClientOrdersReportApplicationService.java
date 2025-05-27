package me.dahiorus.project.vending.application.service.machine;

import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReport;
import me.dahiorus.project.vending.domain.reporting.port.VendingMachineClientOrdersReportApiPort;
import me.dahiorus.project.vending.domain.reporting.usecase.ReportVendingMachineClientOrders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class VendingMachineClientOrdersReportApplicationService
    implements VendingMachineClientOrdersReportApiPort {

  private final ReportVendingMachineClientOrders reportVendingMachineClientOrders;

  public VendingMachineClientOrdersReportApplicationService(
      final ReportVendingMachineClientOrders reportVendingMachineClientOrders) {
    this.reportVendingMachineClientOrders = reportVendingMachineClientOrders;
  }

  @Override
  public VendingMachineClientOrdersReport reportClientOrders(
      final VendingMachineId vendingMachineId) throws ResourceNotFound {
    return reportVendingMachineClientOrders.execute(vendingMachineId);
  }
}
