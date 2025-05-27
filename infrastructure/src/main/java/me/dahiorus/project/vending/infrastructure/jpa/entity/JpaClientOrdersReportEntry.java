package me.dahiorus.project.vending.infrastructure.jpa.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import me.dahiorus.project.vending.domain.item.entity.ItemName;
import me.dahiorus.project.vending.domain.machine.entity.SerialNumber;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReport.ReportedClientOrder;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "reported_client_order")
@AttributeOverride(name = "id", column = @Column(name = "reported_client_order_id"))
@Immutable
public class JpaClientOrdersReportEntry extends JpaEntity {

  @Column(name = "vending_machine_serial_number", nullable = false, updatable = false)
  private String vendingMachineSerialNumber;

  @Column(name = "ordered_item_name", nullable = false, updatable = false)
  private String orderedItemName;

  @Column(name = "ordered_item_price", nullable = false, updatable = false)
  private BigDecimal orderedItemPrice;

  @Column(name = "ordered_at", nullable = false, updatable = false)
  private LocalDateTime orderedAt;

  @ManyToOne
  @JoinColumn(
      name = "reported_client_order_report_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "FK_REPORTED_CLIENT_ORDER_ENTRY_REPORT"))
  private JpaClientOrdersReport ordersReport;

  public void setOrdersReport(final JpaClientOrdersReport ordersReport) {
    this.ordersReport = ordersReport;
  }

  public static JpaClientOrdersReportEntry fromDomain(
      final ReportedClientOrder reportedClientOrder) {
    JpaClientOrdersReportEntry jpaClientOrdersReportEntry = new JpaClientOrdersReportEntry();
    jpaClientOrdersReportEntry.vendingMachineSerialNumber =
        reportedClientOrder.vendingMachineSerialNumber().value();
    jpaClientOrdersReportEntry.orderedItemName = reportedClientOrder.itemName().value();
    jpaClientOrdersReportEntry.orderedItemPrice = reportedClientOrder.itemPrice();
    jpaClientOrdersReportEntry.orderedAt = reportedClientOrder.orderAt();

    return jpaClientOrdersReportEntry;
  }

  public ReportedClientOrder toDomain() {
    return new ReportedClientOrder(
        SerialNumber.of(vendingMachineSerialNumber),
        ItemName.of(orderedItemName),
        orderedItemPrice,
        orderedAt);
  }
}
