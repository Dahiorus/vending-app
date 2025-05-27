package me.dahiorus.project.vending.domain.reporting.port;

import java.util.Optional;
import me.dahiorus.project.vending.domain.CreateSpi;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReport;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReportToCreate;

public interface VendingMachineClientOrdersReportRepositoryPort
    extends CreateSpi<VendingMachineClientOrdersReportToCreate, VendingMachineClientOrdersReport> {

  Optional<VendingMachineClientOrdersReport> findLastGeneratedOf(VendingMachine vendingMachine);
}
