package me.dahiorus.project.vending.infrastructure.jpa.entity;

import static jakarta.persistence.FetchType.LAZY;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import me.dahiorus.project.vending.domain.item.entity.ItemType;
import me.dahiorus.project.vending.domain.machine.entity.SerialNumber;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.CardSystemStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.ChangeSystemStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.PowerStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.Temperature;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.WorkingStatus;
import me.dahiorus.project.vending.domain.stock.entity.VendingMachineStock;

@Entity
@Table(
    name = "vending_machine",
    uniqueConstraints =
        @UniqueConstraint(name = "UK_VENDING_MACHINE_SERIAL_NUMBER", columnNames = "serialNumber"),
    indexes = {
      @Index(
          columnList = "streetNumber, streetName, postalCode, city",
          name = "IDX_VENDING_MACHINE_ADDRESS"),
      @Index(columnList = "latitude, longitude", name = "IDX_VENDING_MACHINE_POSITION"),
      @Index(columnList = "postalCode, city", name = "IDX_VENDING_MACHINE_CITY"),
      @Index(columnList = "type", name = "IDX_VENDING_MACHINE_TYPE"),
      @Index(columnList = "powerStatus", name = "IDX_VENDING_MACHINE_POWER_STATUS"),
      @Index(columnList = "powerStatus, workingStatus", name = "IDX_VENDING_MACHINE_WORKING_STATUS")
    })
@AttributeOverride(name = "id", column = @Column(name = "vending_machine_id"))
public class JpaVendingMachine extends JpaEntity {
  @Column(nullable = false)
  private String serialNumber;

  @Embedded private JpaAddress address = new JpaAddress();

  @Column private LocalDateTime lastIntervention;

  @Column private Integer temperature;

  @Enumerated(EnumType.STRING)
  @Column
  private ItemType type;

  @Enumerated(EnumType.STRING)
  @Column
  private PowerStatus powerStatus;

  @Enumerated(EnumType.STRING)
  @Column
  private WorkingStatus workingStatus;

  @Enumerated(EnumType.STRING)
  @Column
  private CardSystemStatus rfidStatus;

  @Enumerated(EnumType.STRING)
  @Column
  private CardSystemStatus smartCardStatus;

  @Enumerated(EnumType.STRING)
  @Column
  private ChangeSystemStatus changeMoneyStatus;

  @OneToMany(fetch = LAZY, orphanRemoval = true, mappedBy = "vendingMachine")
  private Set<JpaVendingMachineStockEntry> stocks = new LinkedHashSet<>();

  @OneToMany(fetch = LAZY, orphanRemoval = true, mappedBy = "vendingMachine")
  private List<JpaClientOrder> orders = new LinkedList<>();

  public void addOrUpdateStock(JpaVendingMachineStockEntry stock) {
    stocks.stream()
        .filter(currentStock -> currentStock.equals(stock))
        .findFirst()
        .ifPresentOrElse(
            stockToUpdate -> stockToUpdate.setQuantity(stock.getQuantity()),
            () -> {
              stocks.add(stock);
              stock.setVendingMachine(this);
            });
  }

  public void removeStock(JpaVendingMachineStockEntry stockEntry) {
    stockEntry.setVendingMachine(null);
    stocks.removeIf(currentStock -> Objects.equals(currentStock.getId(), stockEntry.getId()));
  }

  public void addOrder(JpaClientOrder order) {
    orders.add(order);
    order.setVendingMachine(this);
  }

  @Override
  public String toString() {
    return super.toString()
        + "[serialNumber="
        + serialNumber
        + ", address="
        + address
        + ", lastIntervention="
        + lastIntervention
        + ", temperature="
        + temperature
        + ", type="
        + type
        + ", powerStatus="
        + powerStatus
        + ", workingStatus="
        + workingStatus
        + ", rfidStatus="
        + rfidStatus
        + ", smartCardStatus="
        + smartCardStatus
        + ", changeMoneyStatus="
        + changeMoneyStatus
        + "]";
  }

  public static JpaVendingMachine fromDomain(VendingMachine vendingMachine) {
    var jpaEntity = new JpaVendingMachine();

    ofNullable(vendingMachine.id()).map(VendingMachineId::value).ifPresent(jpaEntity::setId);
    jpaEntity.serialNumber =
        Optional.ofNullable(vendingMachine.serialNumber()).map(SerialNumber::value).orElse(null);
    jpaEntity.type = vendingMachine.itemType();
    jpaEntity.address =
        Optional.ofNullable(vendingMachine.address()).map(JpaAddress::fromDomain).orElse(null);
    Optional.ofNullable(vendingMachine.status())
        .ifPresent(
            status -> {
              jpaEntity.temperature =
                  Optional.ofNullable(status.temperature()).map(Temperature::value).orElse(null);
              jpaEntity.powerStatus = status.powerStatus();
              jpaEntity.workingStatus = status.workingStatus();
              jpaEntity.rfidStatus = status.rfidStatus();
              jpaEntity.smartCardStatus = status.smartCardStatus();
              jpaEntity.changeMoneyStatus = status.changeMoneyStatus();
            });
    jpaEntity.lastIntervention = vendingMachine.lastIntervention();

    return jpaEntity;
  }

  public VendingMachine toDomain() {
    return new VendingMachine(
        new VendingMachineId(getId()),
        SerialNumber.of(serialNumber),
        address.toDomain(),
        type,
        new VendingMachineStatus(
            Temperature.of(temperature),
            powerStatus,
            workingStatus,
            rfidStatus,
            smartCardStatus,
            changeMoneyStatus),
        lastIntervention);
  }

  public VendingMachineStock toVendingMachineStocks() {
    return new VendingMachineStock(
        stocks.stream().map(JpaVendingMachineStockEntry::toDomain).collect(toSet()));
  }
}
