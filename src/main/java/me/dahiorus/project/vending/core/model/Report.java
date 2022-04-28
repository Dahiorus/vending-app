package me.dahiorus.project.vending.core.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "report", indexes = {
    @Index(name = "IDX_REPORT_MACHINE_SERIAL_NUMBER", columnList = "machineSerialNumber"),
    @Index(name = "IDX_REPORT_CREATED_AT", columnList = "createdAt")
})
@Immutable
public class Report extends AbstractEntity
{
  private String machineSerialNumber;

  private Integer mesuredTemperature;

  private Double totalSaleAmount;

  private PowerStatus powerStatus;

  private WorkingStatus workingStatus;

  private List<ReportStock> reportStocks = new ArrayList<>(0);

  private CardSystemStatus rfidStatus;

  private CardSystemStatus smartCardStatus;

  private ChangeSystemStatus changeMoneyStatus;

  public static Report of(final VendingMachine machine, @Nullable final Instant lastReportingDate)
  {
    Report report = new Report();
    report.machineSerialNumber = machine.getSerialNumber();
    report.mesuredTemperature = machine.getTemperature();
    report.totalSaleAmount = machine.computeTotalAmountSince(lastReportingDate);
    report.powerStatus = machine.getPowerStatus();
    report.workingStatus = machine.getWorkingStatus();
    report.rfidStatus = machine.getRfidStatus();
    report.smartCardStatus = machine.getSmartCardStatus();
    report.changeMoneyStatus = machine.getChangeMoneyStatus();
    report.reportStocks = machine.getStocks()
      .stream()
      .map(ReportStock::of)
      .toList();
    report.reportStocks.forEach(s -> s.setReport(report));

    return report;
  }

  @Column(nullable = false)
  public String getMachineSerialNumber()
  {
    return machineSerialNumber;
  }

  public void setMachineSerialNumber(final String machineSerialNumber)
  {
    this.machineSerialNumber = machineSerialNumber;
  }

  @Column(nullable = false)
  public Integer getMesuredTemperature()
  {
    return mesuredTemperature;
  }

  public void setMesuredTemperature(final Integer mesuredTemperature)
  {
    this.mesuredTemperature = mesuredTemperature;
  }

  @Column(scale = 3)
  public Double getTotalSaleAmount()
  {
    return totalSaleAmount;
  }

  public void setTotalSaleAmount(final Double totalSaleAmount)
  {
    this.totalSaleAmount = totalSaleAmount;
  }

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public PowerStatus getPowerStatus()
  {
    return powerStatus;
  }

  public void setPowerStatus(final PowerStatus powerStatus)
  {
    this.powerStatus = powerStatus;
  }

  @Enumerated(EnumType.STRING)
  @Column(updatable = false)
  public WorkingStatus getWorkingStatus()
  {
    return workingStatus;
  }

  public void setWorkingStatus(final WorkingStatus workingStatus)
  {
    this.workingStatus = workingStatus;
  }

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true, mappedBy = "report")
  public List<ReportStock> getReportStocks()
  {
    return reportStocks;
  }

  public void setReportStocks(final List<ReportStock> reportStocks)
  {
    this.reportStocks = reportStocks;
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
}
