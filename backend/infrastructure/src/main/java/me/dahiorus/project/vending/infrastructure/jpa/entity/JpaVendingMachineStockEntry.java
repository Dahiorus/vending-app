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
import java.util.Objects;
import java.util.UUID;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.stock.entity.ItemQuantity;
import me.dahiorus.project.vending.domain.stock.entity.Quantity;

@Entity
@Table(
    name = "vending_machine_stock",
    indexes = {@Index(name = "IDX_STOCK_VENDING_MACHINE", columnList = "vending_machine_id")})
public class JpaVendingMachineStockEntry {
  @EmbeddedId private JpaStockId id;

  @ManyToOne(fetch = LAZY)
  @MapsId("vendingMachineId")
  @JoinColumn(
      name = "vending_machine_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "FK_STOCK_VENDING_MACHINE"))
  private JpaVendingMachine vendingMachine;

  @ManyToOne(fetch = LAZY)
  @MapsId("itemId")
  @JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(name = "FK_STOCK_ITEM"))
  private JpaItem item;

  @Column(nullable = false)
  private Integer quantity;

  public JpaStockId getId() {
    return id;
  }

  public void setVendingMachine(JpaVendingMachine vendingMachine) {
    this.vendingMachine = vendingMachine;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public static JpaVendingMachineStockEntry fromDomain(
      VendingMachineId vendingMachineId, ItemQuantity itemQuantity) {
    JpaVendingMachineStockEntry stock = new JpaVendingMachineStockEntry();
    stock.id = JpaStockId.of(vendingMachineId, itemQuantity.item().id());
    stock.item = JpaItem.fromDomain(itemQuantity.item());
    stock.quantity = itemQuantity.quantity().value();
    return stock;
  }

  public ItemQuantity toDomain() {
    return new ItemQuantity(item.toDomain(), new Quantity(quantity));
  }

  @Embeddable
  public static class JpaStockId {
    private UUID vendingMachineId;
    private UUID itemId;

    public static JpaStockId of(VendingMachineId vendingMachineId, ItemId itemId) {
      JpaStockId id = new JpaStockId();
      id.vendingMachineId = vendingMachineId.value();
      id.itemId = itemId.value();
      return id;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof JpaStockId that)) {
        return false;
      }
      return Objects.equals(vendingMachineId, that.vendingMachineId)
          && Objects.equals(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(vendingMachineId, itemId);
    }
  }
}
