package me.dahiorus.project.vending.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

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

  private Long quantity;

  public static Stock of(final Item item, final Long quantity)
  {
    Stock stock = new Stock();
    stock.setItem(item);
    stock.setQuantity(quantity);

    return stock;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(name = "FK_STOCK_ITEM"))
  public Item getItem()
  {
    return item;
  }

  public void setItem(final Item item)
  {
    this.item = item;
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

  public void addQuantity(final long quantity)
  {
    this.quantity += quantity;
  }

  public void decrementQuantity()
  {
    this.quantity--;
  }
}
