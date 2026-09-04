package me.dahiorus.project.vending.application.service.machine;

import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.usecase.RepairVendingMachine;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStatusReport;
import me.dahiorus.project.vending.domain.reporting.port.VendingMachineStatusApiPort;
import me.dahiorus.project.vending.domain.reporting.usecase.ReportVendingMachineStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VendingMachineStatusApplicationService implements VendingMachineStatusApiPort {

  private final RepairVendingMachine repairVendingMachine;
  private final ReportVendingMachineStatus reportVendingMachineStatus;

  public VendingMachineStatusApplicationService(
      RepairVendingMachine repairVendingMachine,
      ReportVendingMachineStatus reportVendingMachineStatus) {
    this.repairVendingMachine = repairVendingMachine;
    this.reportVendingMachineStatus = reportVendingMachineStatus;
  }

  @Override
  public VendingMachine resetStatus(VendingMachineId id) throws ResourceNotFound {
    return repairVendingMachine.execute(id);
  }

  @Override
  public VendingMachineStatusReport reportStatus(VendingMachineId id) throws ResourceNotFound {
    return reportVendingMachineStatus.execute(id);
  }
}
