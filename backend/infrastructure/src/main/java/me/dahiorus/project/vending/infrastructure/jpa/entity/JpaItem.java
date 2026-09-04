package me.dahiorus.project.vending.infrastructure.jpa.entity;

import jakarta.persistence.AttributeOverride;
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
import java.math.BigDecimal;
import java.util.Optional;
import me.dahiorus.project.vending.domain.item.entity.Item;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.entity.ItemName;
import me.dahiorus.project.vending.domain.item.entity.ItemToCreate;
import me.dahiorus.project.vending.domain.item.entity.ItemType;

@Entity
@Table(
    name = "item",
    uniqueConstraints = {
      @UniqueConstraint(name = "UK_ITEM_NAME", columnNames = "name"),
      @UniqueConstraint(name = "UK_ITEM_IMAGE", columnNames = "image_id")
    },
    indexes = {
      @Index(name = "IDX_ITEM_TYPE", columnList = "type"),
      @Index(name = "IDS_ITEM_NAME", columnList = "name")
    })
@AttributeOverride(name = "id", column = @Column(name = "item_id"))
public class JpaItem extends JpaEntity {
  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ItemType type;

  @Column(nullable = false, scale = 2, precision = 4)
  private BigDecimal price;

  @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "image_id", foreignKey = @ForeignKey(name = "FK_ITEM_IMAGE_ID"))
  private JpaUploadedFile image;

  public String getName() {
    return name;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setImage(final JpaUploadedFile image) {
    this.image = image;
  }

  public Optional<JpaUploadedFile> maybeImage() {
    return Optional.ofNullable(image);
  }

  @Override
  public String toString() {
    return super.toString() + "[name=" + name + ", type=" + type + ", price=" + price + "]";
  }

  public Item toDomain() {
    return new Item(new ItemId(getId()), ItemName.of(name), price, type);
  }

  public static JpaItem createFrom(final ItemToCreate toCreate) {
    var jpaItem = new JpaItem();
    jpaItem.name = toCreate.name().value();
    jpaItem.price = toCreate.price();
    jpaItem.type = toCreate.type();

    return jpaItem;
  }

  public static JpaItem fromDomain(Item item) {
    var jpaItem = new JpaItem();
    Optional.ofNullable(item.id()).map(ItemId::value).ifPresent(jpaItem::setId);
    jpaItem.name = Optional.ofNullable(item.name()).map(ItemName::value).orElse(null);
    jpaItem.price = item.price();
    jpaItem.type = item.type();

    return jpaItem;
  }
}
