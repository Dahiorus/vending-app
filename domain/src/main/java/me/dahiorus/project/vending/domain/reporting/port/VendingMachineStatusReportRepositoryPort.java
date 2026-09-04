package me.dahiorus.project.vending.domain.reporting.port;

import me.dahiorus.project.vending.domain.Creatable;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStatusReport;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStatusReportToCreate;

public interface VendingMachineStatusReportRepositoryPort
    extends Creatable<VendingMachineStatusReportToCreate, VendingMachineStatusReport> {}
