package me.dahiorus.project.vending.domain.reporting.entity;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import me.dahiorus.project.vending.domain.item.entity.ItemName;
import me.dahiorus.project.vending.domain.machine.entity.SerialNumber;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReport.ReportedClientOrder;
import org.junit.jupiter.api.Test;

class VendingMachineClientOrdersReportTest {

  @Test
  void should_get_total_amount_of_reported_client_orders() {
    var reportedClientOrders =
        List.of(
            new ReportedClientOrder(
                SerialNumber.of("VM-123"), ItemName.of("Item 1"), BigDecimal.valueOf(1.5), now()),
            new ReportedClientOrder(
                SerialNumber.of("VM-123"), ItemName.of("Item 1"), BigDecimal.valueOf(1.5), now()),
            new ReportedClientOrder(
                SerialNumber.of("VM-123"), ItemName.of("Item 2"), BigDecimal.valueOf(2.1), now()),
            new ReportedClientOrder(
                SerialNumber.of("VM-123"), ItemName.of("Item 2"), BigDecimal.valueOf(2.1), now()));
    var report =
        new VendingMachineClientOrdersReport(
            new VendingMachineClientOrdersReportId(UUID.randomUUID()),
            SerialNumber.of("VM-123"),
            reportedClientOrders,
            now());

    var result = report.computeTotalAmount();

    assertThat(result.value()).isEqualByComparingTo(BigDecimal.valueOf(7.2));
  }
}
