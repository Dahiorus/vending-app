package me.dahiorus.project.vending.domain.reporting.entity;

import java.time.LocalDateTime;
import java.util.Set;
import me.dahiorus.project.vending.domain.item.entity.ItemName;
import me.dahiorus.project.vending.domain.machine.entity.SerialNumber;
import me.dahiorus.project.vending.domain.stock.entity.ItemQuantity;
import me.dahiorus.project.vending.domain.stock.entity.Quantity;

public record VendingMachineStockReport(
    VendingMachineStockReportId id,
    SerialNumber serialNumber,
    Set<ReportedStockEntry> reportedStockEntries,
    LocalDateTime reportedAt) {
  public String serialNumberAsString() {
    return serialNumber.value();
  }

  public record ReportedStockEntry(ItemName itemName, Quantity quantity) {
    public static ReportedStockEntry from(ItemQuantity itemQuantity) {
      return new ReportedStockEntry(itemQuantity.itemName(), itemQuantity.quantity());
    }
  }
}
