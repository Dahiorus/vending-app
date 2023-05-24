package me.dahiorus.project.vending.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "vending_machine",
  uniqueConstraints = @UniqueConstraint(name = "UK_VENDING_MACHINE_SERIAL_NUMBER",
    columnNames = "serialNumber"),
  indexes = { @Index(columnList = "streetAddress", name = "IDX_VENDING_MACHINE_ADDRESS"),
    @Index(columnList = "latitude, longitude", name = "IDX_VENDING_MACHINE_POSITION"),
    @Index(columnList = "place", name = "IDX_VENDING_MACHINE_PLACE"),
    @Index(columnList = "type", name = "IDX_VENDING_MACHINE_TYPE"),
    @Index(columnList = "powerStatus", name = "IDX_VENDING_MACHINE_POWER_STATUS"), @Index(
      columnList = "powerStatus, workingStatus", name = "IDX_VENDING_MACHINE_WORKING_STATUS") })
public class VendingMachine extends AbstractEntity
{
  private String serialNumber;

  private Address address = new Address();

  private Instant lastIntervention;

  private Integer temperature;

  private ItemType type;

  private PowerStatus powerStatus;

  private WorkingStatus workingStatus;

  private CardSystemStatus rfidStatus;

  private CardSystemStatus smartCardStatus;

  private ChangeSystemStatus changeMoneyStatus;

  private List<Comment> comments = new LinkedList<>();

  private List<Stock> stocks = new LinkedList<>();

  private List<Sale> sales = new LinkedList<>();

  @Column(nullable = false)
  public String getSerialNumber()
  {
    return serialNumber;
  }

  public void setSerialNumber(final String serialNumber)
  {
    this.serialNumber = serialNumber;
  }

  @Embedded
  public Address getAddress()
  {
    return address;
  }

  public void setAddress(final Address address)
  {
    this.address = address;
  }

  @Column
  public Instant getLastIntervention()
  {
    return lastIntervention;
  }

  public void setLastIntervention(final Instant lastIntervention)
  {
    this.lastIntervention = lastIntervention;
  }

  public void markIntervention()
  {
    setLastIntervention(Instant.now());
  }

  @Column
  public Integer getTemperature()
  {
    return temperature;
  }

  public void setTemperature(final Integer temperature)
  {
    this.temperature = temperature;
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

  @Enumerated(EnumType.STRING)
  @Column
  public PowerStatus getPowerStatus()
  {
    return powerStatus;
  }

  public void setPowerStatus(final PowerStatus powerStatus)
  {
    this.powerStatus = powerStatus;
  }

  @Transient
  public boolean isPowered()
  {
    return powerStatus == PowerStatus.ON;
  }

  @Enumerated(EnumType.STRING)
  @Column
  public WorkingStatus getWorkingStatus()
  {
    return workingStatus;
  }

  public void setWorkingStatus(final WorkingStatus workingStatus)
  {
    this.workingStatus = workingStatus;
  }

  @Transient
  public boolean isWorking()
  {
    return isPowered() && workingStatus == WorkingStatus.OK;
  }

  @Enumerated(EnumType.STRING)
  @Column
  public CardSystemStatus getRfidStatus()
  {
    return rfidStatus;
  }

  public void setRfidStatus(final CardSystemStatus rfidStatus)
  {
    this.rfidStatus = rfidStatus;
  }

  @Enumerated(EnumType.STRING)
  @Column
  public CardSystemStatus getSmartCardStatus()
  {
    return smartCardStatus;
  }

  public void setSmartCardStatus(final CardSystemStatus smartCardStatus)
  {
    this.smartCardStatus = smartCardStatus;
  }

  @Enumerated(EnumType.STRING)
  @Column
  public ChangeSystemStatus getChangeMoneyStatus()
  {
    return changeMoneyStatus;
  }

  @Transient
  public boolean isAllSystemClear()
  {
    return isWorking() && rfidStatus == CardSystemStatus.NORMAL &&
      smartCardStatus == CardSystemStatus.NORMAL && changeMoneyStatus == ChangeSystemStatus.NORMAL;
  }

  public void setChangeMoneyStatus(final ChangeSystemStatus changeMoneyStatus)
  {
    this.changeMoneyStatus = changeMoneyStatus;
  }

  @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "vending_machine_id",
    foreignKey = @ForeignKey(name = "FK_VENDING_MACHINE_COMMENT"), updatable = false,
    nullable = false)
  @OrderBy(value = "createdAt DESC")
  public List<Comment> getComments()
  {
    return comments;
  }

  public void setComments(final List<Comment> comments)
  {
    this.comments = comments;
  }

  public void addComment(final Comment comment)
  {
    getComments().add(comment);
  }

  @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "vending_machine_id", nullable = false, updatable = false,
    foreignKey = @ForeignKey(name = "FK_STOCK_VENDING_MACHINE"))
  public List<Stock> getStocks()
  {
    return stocks;
  }

  public void setStocks(final List<Stock> stocks)
  {
    this.stocks = stocks;
  }

  public Optional<Stock> findStock(final Item item)
  {
    return stocks.stream()
      .filter(stock -> Objects.equals(stock.getItem(), item))
      .findFirst();
  }

  public void addStock(final Stock stock)
  {
    stocks.add(stock);
  }

  public boolean hasStock(@Nonnull final Item item)
  {
    return getQuantityInStock(item) > 0;
  }

  public long getQuantityInStock(final Item item)
  {
    return findStock(item).map(Stock::getQuantity)
      .orElse(0);
  }

  @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "machine")
  @OrderBy(value = "createdAt DESC")
  public List<Sale> getSales()
  {
    return sales;
  }

  public void setSales(final List<Sale> sales)
  {
    this.sales = sales;
  }

  public void addSale(final Sale sale)
  {
    sales.add(sale);
  }

  public BigDecimal computeTotalAmountSince(final Instant lastReportingDate)
  {
    if (lastReportingDate == null)
    {
      return sales.stream()
        .map(Sale::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    return sales.stream()
      .filter(s -> lastReportingDate.isBefore(s.getCreatedAt()))
      .map(Sale::getAmount)
      .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Override
  public String toString()
  {
    return super.toString() + "[serialNumber=" + serialNumber + ", address=" + address +
      ", lastIntervention=" + lastIntervention + ", temperature=" + temperature + ", type=" + type +
      ", powerStatus=" + powerStatus + ", workingStatus=" + workingStatus + ", rfidStatus=" +
      rfidStatus + ", smartCardStatus=" + smartCardStatus + ", changeMoneyStatus=" +
      changeMoneyStatus + "]";
  }
}
