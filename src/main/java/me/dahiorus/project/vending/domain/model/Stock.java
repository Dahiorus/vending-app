package me.dahiorus.project.vending.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "stock",
  uniqueConstraints = @UniqueConstraint(name = "UK_STOCK_MACHINE_ITEM",
    columnNames = { "vending_machine_id", "item_id" }),
  indexes = {
    @Index(name = "IDX_STOCK_VENDING_MACHINE", columnList = "vending_machine_id")
  })
public class Stock extends AbstractEntity
{
  private Item item;

  private Integer quantity;

  public static Stock fill(final Item item, final Integer quantity)
  {
    Stock stock = new Stock();
    stock.setItem(item);
    stock.setQuantity(quantity);

    return stock;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "item_id", nullable = false, updatable = false, foreignKey = @ForeignKey(name = "FK_STOCK_ITEM"))
  public Item getItem()
  {
    return item;
  }

  public void setItem(final Item item)
  {
    this.item = item;
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

  public void addQuantity(final int quantity)
  {
    this.quantity += quantity;
  }

  public void decrementQuantity()
  {
    this.quantity--;
  }
}
