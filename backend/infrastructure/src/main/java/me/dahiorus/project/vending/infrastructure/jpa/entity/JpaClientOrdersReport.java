package me.dahiorus.project.vending.infrastructure.jpa.entity;

import static jakarta.persistence.CascadeType.PERSIST;
import static java.util.Comparator.comparing;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.LinkedList;
import java.util.List;
import me.dahiorus.project.vending.domain.machine.entity.SerialNumber;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReport;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReport.ReportedClientOrder;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReportId;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReportToCreate;
import org.hibernate.annotations.Immutable;

@Entity
@Table(
    name = "client_orders_report",
    indexes = {
      @Index(
          name = "IDX_CLIENT_ORDERS_REPORT_VENDING_MACHINE_SERIAL_NUMBER",
          columnList = "vendingMachineSerialNumber"),
      @Index(name = "IDX_CLIENT_ORDERS_REPORT_CREATED_AT", columnList = "createdAt")
    })
@AttributeOverride(name = "id", column = @Column(name = "client_orders_report_id"))
@Immutable
public class JpaClientOrdersReport extends JpaEntity {
  @Column(name = "vending_machine_serial_number", nullable = false)
  private String vendingMachineSerialNumber;

  @OneToMany(cascade = PERSIST, orphanRemoval = true, mappedBy = "ordersReport")
  private List<JpaClientOrdersReportEntry> reportedClientOrders = new LinkedList<>();

  public void addOrder(JpaClientOrdersReportEntry order) {
    reportedClientOrders.add(order);
    order.setOrdersReport(this);
  }

  public static JpaClientOrdersReport createFrom(
      final VendingMachineClientOrdersReportToCreate reportToCreate) {
    JpaClientOrdersReport report = new JpaClientOrdersReport();
    report.vendingMachineSerialNumber = reportToCreate.vendingMachineSerialNumber().value();
    reportToCreate.clientOrders().stream()
        .map(ReportedClientOrder::from)
        .map(JpaClientOrdersReportEntry::fromDomain)
        .forEach(report::addOrder);

    return report;
  }

  public VendingMachineClientOrdersReport toDomain() {
    return new VendingMachineClientOrdersReport(
        new VendingMachineClientOrdersReportId(getId()),
        SerialNumber.of(vendingMachineSerialNumber),
        reportedClientOrders.stream()
            .map(JpaClientOrdersReportEntry::toDomain)
            .sorted(comparing(ReportedClientOrder::orderAt).reversed())
            .toList(),
        getCreatedAt());
  }
}
