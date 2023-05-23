package me.dahiorus.project.vending.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "report_stock", indexes = @Index(name = "IDX_REPORT_STOCK_ITEM_NAME", columnList = "itemName"))
@Immutable
public class ReportStock extends AbstractEntity
{
  private String itemName;

  private Integer quantity;

  private Report report;

  public static ReportStock report(final Stock stock)
  {
    ReportStock reportStock = new ReportStock();
    reportStock.itemName = stock.getItem()
      .getName();
    reportStock.quantity = stock.getQuantity();

    return reportStock;
  }

  @Column(nullable = false)
  public String getItemName()
  {
    return itemName;
  }

  public void setItemName(final String itemName)
  {
    this.itemName = itemName;
  }

  @Column(nullable = false)
  public Integer getQuantity()
  {
    return quantity;
  }

  public void setQuantity(final Integer quantity)
  {
    this.quantity = quantity;
  }

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "report_id", nullable = false, updatable = false,
    foreignKey = @ForeignKey(name = "FK_REPORT_STOCK_ID"))
  public Report getReport()
  {
    return report;
  }

  public void setReport(final Report report)
  {
    this.report = report;
  }
}
