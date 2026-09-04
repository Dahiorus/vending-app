package me.dahiorus.project.vending.infrastructure.jpa.entity;

import static jakarta.persistence.FetchType.LAZY;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.entity.ItemName;
import me.dahiorus.project.vending.domain.machine.entity.ClientOrder;
import me.dahiorus.project.vending.domain.machine.entity.ClientOrder.OrderedItem;
import me.dahiorus.project.vending.domain.machine.entity.ClientOrderId;

@Entity
@Table(
    name = "vending_machine_order",
    indexes = {
      @Index(name = "IDX_ORDER_VENDING_MACHINE", columnList = "vending_machine_id"),
      @Index(name = "IDX_ORDER_ITEM", columnList = "ordered_item_id")
    })
@AttributeOverride(name = "id", column = @Column(name = "vending_machine_order_id"))
public class JpaClientOrder extends JpaEntity {
  @Column(nullable = false, updatable = false)
  private UUID orderedItemId;

  @Column(nullable = false, updatable = false)
  private String orderedItemName;

  @Column(nullable = false, scale = 2, precision = 4)
  private BigDecimal orderedItemPrice;

  @ManyToOne(fetch = LAZY, optional = false)
  @JoinColumn(
      name = "vending_machine_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "FK_ORDER_VENDING_MACHINE"))
  private JpaVendingMachine vendingMachine;

  public void setVendingMachine(final JpaVendingMachine vendingMachine) {
    this.vendingMachine = vendingMachine;
  }

  public void setItemDetails(final JpaItem item) {
    orderedItemId = item.getId();
    orderedItemName = item.getName();
    orderedItemPrice = item.getPrice();
  }

  public void setOrderAt(final LocalDateTime orderAt) {
    setCreatedAt(orderAt);
  }

  public ClientOrder toDomain() {
    return new ClientOrder(
        new ClientOrderId(getId()),
        vendingMachine.toDomain(),
        new OrderedItem(new ItemId(orderedItemId), ItemName.of(orderedItemName), orderedItemPrice),
        getCreatedAt());
  }

  @Override
  public String toString() {
    return "JpaClientOrder["
        + "vendingMachineId="
        + vendingMachine.getId()
        + ", orderedItemId="
        + orderedItemId
        + ", orderedItemName='"
        + orderedItemName
        + "', orderedItemPrice="
        + orderedItemPrice
        + ", orderedAt='"
        + getCreatedAt()
        + "'"
        + ']';
  }
}
