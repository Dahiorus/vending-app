package me.dahiorus.project.vending.domain.reporting.entity;

import java.time.LocalDateTime;
import me.dahiorus.project.vending.domain.machine.entity.SerialNumber;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus;

public record VendingMachineStatusReport(
    VendingMachineStatusReportId id,
    SerialNumber serialNumber,
    LocalDateTime lastIntervention,
    VendingMachineStatus status,
    LocalDateTime reportedAt) {}
