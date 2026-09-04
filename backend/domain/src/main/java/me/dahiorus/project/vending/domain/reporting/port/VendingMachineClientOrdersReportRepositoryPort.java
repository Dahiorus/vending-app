package me.dahiorus.project.vending.domain.reporting.port;

import java.util.Optional;
import me.dahiorus.project.vending.domain.Creatable;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReport;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReportToCreate;

public interface VendingMachineClientOrdersReportRepositoryPort
    extends Creatable<VendingMachineClientOrdersReportToCreate, VendingMachineClientOrdersReport> {

  Optional<VendingMachineClientOrdersReport> findLastGeneratedOf(VendingMachine vendingMachine);
}
