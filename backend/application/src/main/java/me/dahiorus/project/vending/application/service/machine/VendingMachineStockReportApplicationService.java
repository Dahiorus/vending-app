package me.dahiorus.project.vending.application.service.machine;

import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStockReport;
import me.dahiorus.project.vending.domain.reporting.port.VendingMachineStockReportApiPort;
import me.dahiorus.project.vending.domain.reporting.usecase.ReportVendingMachineStock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class VendingMachineStockReportApplicationService
    implements VendingMachineStockReportApiPort {

  private final ReportVendingMachineStock reportVendingMachineStock;

  public VendingMachineStockReportApplicationService(
      ReportVendingMachineStock reportVendingMachineStock) {
    this.reportVendingMachineStock = reportVendingMachineStock;
  }

  @Override
  public VendingMachineStockReport reportStock(VendingMachineId vendingMachineId) {
    return reportVendingMachineStock.execute(vendingMachineId);
  }
}
