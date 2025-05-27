package me.dahiorus.project.vending.infrastructure.jpa.entity;

import static jakarta.persistence.CascadeType.PERSIST;
import static java.util.stream.Collectors.toSet;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import me.dahiorus.project.vending.domain.machine.entity.SerialNumber;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStockReport;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStockReportId;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStockReportToCreate;
import org.hibernate.annotations.Immutable;

@Entity
@Table(
    name = "stock_report",
    indexes = {
      @Index(
          name = "IDX_STOCK_REPORT_VENDING_MACHINE_SERIAL_NUMBER",
          columnList = "vending_machine_serial_number"),
      @Index(name = "IDX_STOCK_REPORT_CREATED_AT", columnList = "created_at")
    })
@AttributeOverride(name = "id", column = @Column(name = "stock_report_id"))
@Immutable
public class JpaStockReport extends JpaEntity {

  @Column(name = "vending_machine_serial_number", nullable = false)
  private String vendingMachineSerialNumber;

  @OneToMany(mappedBy = "stockReport", orphanRemoval = true, cascade = PERSIST)
  private Set<JpaStockReportEntry> reportedItemQuantities = new LinkedHashSet<>();

  private void addReportedItemQuantity(JpaStockReportEntry itemQuantity) {
    this.reportedItemQuantities.add(itemQuantity);
    itemQuantity.setStockReport(this);
  }

  public static JpaStockReport createFrom(VendingMachineStockReportToCreate toCreate) {
    JpaStockReport stockReport = new JpaStockReport();
    stockReport.vendingMachineSerialNumber = toCreate.serialNumber().value();
    toCreate.reportedStock().stream()
        .map(JpaStockReportEntry::fromDomain)
        .forEach(stockReport::addReportedItemQuantity);

    return stockReport;
  }

  public VendingMachineStockReport toDomain() {
    return new VendingMachineStockReport(
        new VendingMachineStockReportId(getId()),
        SerialNumber.of(vendingMachineSerialNumber),
        reportedItemQuantities.stream().map(JpaStockReportEntry::toDomain).collect(toSet()),
        getCreatedAt());
  }
}
