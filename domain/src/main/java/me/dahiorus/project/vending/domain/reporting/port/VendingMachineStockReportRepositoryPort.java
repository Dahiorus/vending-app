package me.dahiorus.project.vending.domain.reporting.port;

import me.dahiorus.project.vending.domain.CreateSpi;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStockReport;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStockReportToCreate;

public interface VendingMachineStockReportRepositoryPort
    extends CreateSpi<VendingMachineStockReportToCreate, VendingMachineStockReport> {}
