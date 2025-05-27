package me.dahiorus.project.vending.infrastructure.rest.entity.machine;


import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReport;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReport.ReportedClientOrder;

public record VendingMachineClientOrdersReportDto(
    @JsonIgnore UUID id,
    String serialNumber,
    List<ReportedClientOrderDto> clientOrders,
    BigDecimal totalAmount,
    LocalDateTime reportedAt) {

  public static VendingMachineClientOrdersReportDto fromDomain(
      final VendingMachineClientOrdersReport report) {
    return new VendingMachineClientOrdersReportDto(
        report.id().value(),
        report.serialNumber().value(),
        report.clientOrders().stream().map(ReportedClientOrderDto::fromDomain).toList(),
        report.computeTotalAmount().value(),
        report.reportedAt());
  }

  public record ReportedClientOrderDto(
      String itemName, BigDecimal itemPrice, LocalDateTime orderedAt) {
    public static ReportedClientOrderDto fromDomain(final ReportedClientOrder reportedClientOrder) {
      return new ReportedClientOrderDto(
          reportedClientOrder.itemName().value(),
          reportedClientOrder.itemPrice(),
          reportedClientOrder.orderAt());
    }
  }
}
