package me.dahiorus.project.vending.core.model;

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

  private Long quantity;

  private Report report;

  public ReportStock()
  {
    // default constructor
  }

  public ReportStock(final Stock stock)
  {
    this.itemName = stock.getItem()
      .getName();
    this.quantity = stock.getQuantity();
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
  public Long getQuantity()
  {
    return quantity;
  }

  public void setQuantity(final Long quantity)
  {
    this.quantity = quantity;
  }

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "report_id", nullable = false, foreignKey = @ForeignKey(name = "FK_REPORT_STOCK_ID"))
  public Report getReport()
  {
    return report;
  }

  public void setReport(final Report report)
  {
    this.report = report;
  }
}
