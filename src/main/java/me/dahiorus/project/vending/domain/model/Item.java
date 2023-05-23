package me.dahiorus.project.vending.domain.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "item",
  uniqueConstraints = @UniqueConstraint(name = "UK_ITEM_NAME", columnNames = "name"),
  indexes = {
    @Index(name = "IDX_ITEM_TYPE", columnList = "type"),
    @Index(name = "IDS_ITEM_NAME", columnList = "name")
  })
public class Item extends AbstractEntity
{
  private String name;

  private ItemType type;

  private BigDecimal price;

  private BinaryData picture;

  @Column(nullable = false)
  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public ItemType getType()
  {
    return type;
  }

  public void setType(final ItemType type)
  {
    this.type = type;
  }

  @Column(nullable = false, scale = 2, precision = 4)
  public BigDecimal getPrice()
  {
    return price;
  }

  public void setPrice(final BigDecimal price)
  {
    this.price = price;
  }

  @OneToOne(fetch = FetchType.LAZY, optional = true, orphanRemoval = true)
  @JoinColumn(name = "picture_id", foreignKey = @ForeignKey(name = "FK_ITEM_PICTURE_ID"))
  public BinaryData getPicture()
  {
    return picture;
  }

  public void setPicture(final BinaryData picture)
  {
    this.picture = picture;
  }

  @Override
  public String toString()
  {
    return super.toString() + "[name=" + name + ", type=" + type + ", price=" + price + "]";
  }
}
