package me.dahiorus.project.vending.core.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "vending_machine",
    uniqueConstraints = @UniqueConstraint(name = "UK_VENDING_MACHINE_SERIAL_NUMBER", columnNames = "serialNumber"),
    indexes = {
        @Index(columnList = "address", name = "IDX_VENDING_MACHINE_ADDRESS"),
        @Index(columnList = "latitude, longitude", name = "IDX_VENDING_MACHINE_POSITION"),
        @Index(columnList = "place", name = "IDX_VENDING_MACHINE_PLACE"),
        @Index(columnList = "type", name = "IDX_VENDING_MACHINE_TYPE"),
        @Index(columnList = "powerStatus", name = "IDX_VENDING_MACHINE_POWER_STATUS"),
        @Index(columnList = "powerStatus, workingStatus", name = "IDX_VENDING_MACHINE_WORKING_STATUS")
    })
public class VendingMachine extends AbstractEntity
{
  private String serialNumber;

  private Double latitude;

  private Double longitude;

  private String place;

  private String address;

  private Instant lastIntervention;

  private Integer temperature;

  private ItemType type;

  private PowerStatus powerStatus;

  private WorkingStatus workingStatus;

  private CardSystemStatus rfidStatus;

  private CardSystemStatus smartCardStatus;

  private ChangeSystemStatus changeMoneyStatus;

  private List<Comment> comments = new ArrayList<>(0);

  private List<Stock> stocks = new ArrayList<>(0);

  private List<Sale> sales = new ArrayList<>(0);

  @Column(nullable = false)
  public String getSerialNumber()
  {
    return serialNumber;
  }

  public void setSerialNumber(final String serialNumber)
  {
    this.serialNumber = serialNumber;
  }

  @Column(precision = 7)
  public Double getLatitude()
  {
    return latitude;
  }

  public void setLatitude(final Double latitude)
  {
    this.latitude = latitude;
  }

  @Column(precision = 7)
  public Double getLongitude()
  {
    return longitude;
  }

  public void setLongitude(final Double longitude)
  {
    this.longitude = longitude;
  }

  @Column
  public String getPlace()
  {
    return place;
  }

  public void setPlace(final String place)
  {
    this.place = place;
  }

  @Column(nullable = false)
  public String getAddress()
  {
    return address;
  }

  public void setAddress(final String address)
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

  public void setChangeMoneyStatus(final ChangeSystemStatus changeMoneyStatus)
  {
    this.changeMoneyStatus = changeMoneyStatus;
  }

  @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name = "vending_machine_id", foreignKey = @ForeignKey(name = "FK_VENDING_MACHINE_COMMENT"),
      updatable = false, nullable = false)
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

  @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name = "vending_machine_id", nullable = false,
      foreignKey = @ForeignKey(name = "FK_STOCK_VENDING_MACHINE"))
  public List<Stock> getStocks()
  {
    return stocks;
  }

  public void setStocks(final List<Stock> stocks)
  {
    this.stocks = stocks;
  }

  public boolean hasItem(@Nonnull final Item item)
  {
    return getStocks().stream()
      .anyMatch(stock -> Objects.equals(stock.getItem(), item));
  }

  public void provision(@Nonnull final Item item, final Integer quantity)
  {
    if (!hasItem(item))
    {
      stocks.add(Stock.of(item, quantity));
    }
    else
    {
      stocks.stream()
        .filter(s -> Objects.equals(s.getItem(), item))
        .findFirst()
        .ifPresent(s -> s.addQuantity(quantity));
    }
  }

  public Sale purchase(@Nonnull final Item item)
  {
    getStocks()
      .stream()
      .filter(stock -> Objects.equals(stock.getItem(), item))
      .findFirst()
      .ifPresent(Stock::decrementQuantity);

    Sale sale = Sale.of(item, this);
    sale.setId(UUID.randomUUID());
    sales.add(sale);

    return sale;
  }

  public long getQuantityInStock(final Item item)
  {
    return stocks.stream()
      .filter(stock -> Objects.equals(stock.getItem(), item))
      .findFirst()
      .map(Stock::getQuantity)
      .orElse(0);
  }

  @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "machine", cascade = CascadeType.MERGE)
  @OrderBy(value = "createdAt DESC")
  public List<Sale> getSales()
  {
    return sales;
  }

  public void setSales(final List<Sale> sales)
  {
    this.sales = sales;
  }

  public Double computeTotalAmountSince(final Instant lastReportingDate)
  {
    if (lastReportingDate == null)
    {
      return sales.stream()
        .mapToDouble(Sale::getAmount)
        .sum();
    }

    return sales.stream()
      .filter(s -> lastReportingDate.isBefore(s.getCreatedAt()))
      .mapToDouble(Sale::getAmount)
      .sum();
  }

  @Override
  public String toString()
  {
    return super.toString() + "[serialNumber=" + serialNumber + ", latitude=" + latitude + ", longitude=" + longitude
        + ", place=" + place + ", address=" + address + ", lastIntervention=" + lastIntervention + ", temperature="
        + temperature + ", type=" + type + ", powerStatus=" + powerStatus + ", workingStatus=" + workingStatus
        + ", rfidStatus=" + rfidStatus + ", smartCardStatus=" + smartCardStatus + ", changeMoneyStatus="
        + changeMoneyStatus + "]";
  }
}
