package me.dahiorus.project.vending.infrastructure.jpa.entity;

import static jakarta.persistence.FetchType.LAZY;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import me.dahiorus.project.vending.domain.item.entity.ItemName;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStockReport.ReportedStockEntry;
import me.dahiorus.project.vending.domain.stock.entity.ItemQuantity;
import me.dahiorus.project.vending.domain.stock.entity.Quantity;

@Entity
@Table(
    name = "stock_report_item_quantity",
    indexes = {@Index(name = "IDX_STOCK_REPORT_ENTRY_STOCK", columnList = "stock_report_id")})
public class JpaStockReportEntry {

  @EmbeddedId private JpaReportedItemQuantityId id = new JpaReportedItemQuantityId();

  @Column(name = "item_name", nullable = false, insertable = false, updatable = false)
  private String itemName;

  @Column(nullable = false)
  private Integer quantityValue;

  @ManyToOne(fetch = LAZY, optional = false)
  @MapsId("stockReportId")
  @JoinColumn(
      name = "stock_report_id",
      insertable = false,
      updatable = false,
      foreignKey = @ForeignKey(name = "FK_STOCK_REPORT_ITEM_QUANTITY_REPORT"))
  private JpaStockReport stockReport;

  public void setStockReport(final JpaStockReport stockReport) {
    this.stockReport = stockReport;
  }

  public static JpaStockReportEntry fromDomain(ItemQuantity itemQuantity) {
    var jpaStockReportEntry = new JpaStockReportEntry();
    var itemName = itemQuantity.itemName().value();
    jpaStockReportEntry.id = JpaReportedItemQuantityId.of(itemName);
    jpaStockReportEntry.itemName = itemName;
    jpaStockReportEntry.quantityValue = itemQuantity.quantityValue();

    return jpaStockReportEntry;
  }

  public ReportedStockEntry toDomain() {
    return new ReportedStockEntry(ItemName.of(itemName), Quantity.of(quantityValue));
  }

  @Embeddable
  public static class JpaReportedItemQuantityId implements Serializable {
    private UUID stockReportId;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    public static JpaReportedItemQuantityId of(String itemName) {
      var id = new JpaReportedItemQuantityId();
      id.itemName = itemName;
      return id;
    }

    @Override
    public boolean equals(final Object o) {
      if (!(o instanceof JpaReportedItemQuantityId that)) {
        return false;
      }
      return Objects.equals(stockReportId, that.stockReportId)
          && Objects.equals(itemName, that.itemName);
    }

    @Override
    public int hashCode() {
      return Objects.hash(stockReportId, itemName);
    }
  }
}
