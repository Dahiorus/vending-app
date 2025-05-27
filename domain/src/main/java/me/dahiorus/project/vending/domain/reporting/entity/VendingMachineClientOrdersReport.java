package me.dahiorus.project.vending.domain.reporting.entity;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import me.dahiorus.project.vending.domain.item.entity.ItemName;
import me.dahiorus.project.vending.domain.machine.entity.ClientOrder;
import me.dahiorus.project.vending.domain.machine.entity.SerialNumber;

public record VendingMachineClientOrdersReport(
    VendingMachineClientOrdersReportId id,
    SerialNumber serialNumber,
    List<ReportedClientOrder> clientOrders,
    LocalDateTime reportedAt) {

  public TotalAmount computeTotalAmount() {
    return TotalAmount.of(
        clientOrders.stream().map(ReportedClientOrder::itemPrice).reduce(ZERO, BigDecimal::add));
  }

  public record TotalAmount(BigDecimal value) {
    public static TotalAmount of(BigDecimal value) {
      return new TotalAmount(value);
    }
  }

  public record ReportedClientOrder(
      SerialNumber vendingMachineSerialNumber,
      ItemName itemName,
      BigDecimal itemPrice,
      LocalDateTime orderAt) {

    public static ReportedClientOrder from(ClientOrder clientOrder) {
      return new ReportedClientOrder(
          clientOrder.vendingMachine().serialNumber(),
          clientOrder.orderedItemName(),
          clientOrder.orderedItemPrice(),
          clientOrder.orderAt());
    }
  }
}
