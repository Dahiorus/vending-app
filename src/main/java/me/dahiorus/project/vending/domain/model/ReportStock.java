package me.dahiorus.project.vending.domain.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "report_stock", indexes = @Index(name = "IDX_REPORT_STOCK_ITEM_NAME", columnList = "itemName"))
@Immutable
public class ReportStock extends AbstractEntity
{
  private String itemName;

  private Integer quantity;

  private Report report;

  public static ReportStock of(final Stock stock)
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
