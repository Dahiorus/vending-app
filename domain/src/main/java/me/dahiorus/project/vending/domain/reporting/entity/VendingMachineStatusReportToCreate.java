package me.dahiorus.project.vending.domain.reporting.entity;

import java.time.LocalDateTime;
import me.dahiorus.project.vending.domain.machine.entity.SerialNumber;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus;

public record VendingMachineStatusReportToCreate(
    SerialNumber serialNumber, LocalDateTime lastIntervention, VendingMachineStatus status) {}
