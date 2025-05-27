package me.dahiorus.project.vending.domain.reporting.entity;

import java.util.UUID;
import me.dahiorus.project.vending.domain.DomainId;

public record VendingMachineStockReportId(UUID value) implements DomainId {}
