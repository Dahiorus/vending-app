package me.dahiorus.project.vending.domain.reporting.entity;

import me.dahiorus.project.vending.domain.machine.entity.SerialNumber;
import me.dahiorus.project.vending.domain.stock.entity.VendingMachineStock;

public record VendingMachineStockReportToCreate(
    SerialNumber serialNumber, VendingMachineStock reportedStock) {}
