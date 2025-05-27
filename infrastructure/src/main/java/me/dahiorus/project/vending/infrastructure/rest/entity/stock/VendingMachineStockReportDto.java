package me.dahiorus.project.vending.infrastructure.rest.entity.stock;

import static java.util.stream.Collectors.toCollection;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStockReport;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStockReport.ReportedStockEntry;

public record VendingMachineStockReportDto(
    UUID id, String serialNumber, Set<ReportedStockEntryDto> stockEntries) {

  public static VendingMachineStockReportDto fromDomain(
      VendingMachineStockReport vendingMachineStockReport) {
    return new VendingMachineStockReportDto(
        vendingMachineStockReport.id().value(),
        vendingMachineStockReport.serialNumberAsString(),
        vendingMachineStockReport.reportedStockEntries().stream()
            .map(ReportedStockEntryDto::fromDomain)
            .collect(toCollection(LinkedHashSet::new)));
  }

  public record ReportedStockEntryDto(String itemName, Integer quantity) {
    public static ReportedStockEntryDto fromDomain(ReportedStockEntry itemQuantity) {
      return new ReportedStockEntryDto(
          itemQuantity.itemName().value(), itemQuantity.quantity().value());
    }
  }
}
