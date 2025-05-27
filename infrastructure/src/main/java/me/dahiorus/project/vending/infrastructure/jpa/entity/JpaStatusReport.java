package me.dahiorus.project.vending.infrastructure.jpa.entity;

import static jakarta.persistence.EnumType.*;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Optional;
import me.dahiorus.project.vending.domain.machine.entity.SerialNumber;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.CardSystemStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.ChangeSystemStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.PowerStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.Temperature;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.WorkingStatus;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStatusReport;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStatusReportId;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStatusReportToCreate;
import org.hibernate.annotations.Immutable;

@Entity
@Table(
    name = "status_report",
    indexes = {
      @Index(
          name = "IDX_STATUS_REPORT_VENDING_MACHINE_SERIAL_NUMBER",
          columnList = "vendingMachineSerialNumber"),
      @Index(name = "IDX_STATUS_REPORT_CREATED_AT", columnList = "createdAt")
    })
@AttributeOverride(name = "id", column = @Column(name = "status_report_id"))
@Immutable
public class JpaStatusReport extends JpaEntity {

  @Column(nullable = false)
  private String vendingMachineSerialNumber;

  @Column(nullable = false)
  private LocalDateTime lastIntervention;

  @Column(nullable = false)
  private Integer measuredTemperature;

  @Enumerated(STRING)
  @Column(nullable = false)
  private PowerStatus powerStatus;

  @Enumerated(STRING)
  @Column(nullable = false)
  private WorkingStatus workingStatus;

  @Enumerated(STRING)
  @Column(nullable = false)
  private CardSystemStatus rfidStatus;

  @Enumerated(STRING)
  @Column(nullable = false)
  private CardSystemStatus smartCardStatus;

  @Enumerated(STRING)
  @Column(nullable = false)
  private ChangeSystemStatus changeMoneyStatus;

  public static JpaStatusReport createFrom(VendingMachineStatusReportToCreate statusReport) {
    var jpaStatusReport = new JpaStatusReport();
    var vendingMachineStatus = statusReport.status();
    jpaStatusReport.measuredTemperature =
        Optional.ofNullable(vendingMachineStatus.temperature())
            .map(Temperature::value)
            .orElse(null);
    jpaStatusReport.lastIntervention = statusReport.lastIntervention();
    jpaStatusReport.powerStatus = vendingMachineStatus.powerStatus();
    jpaStatusReport.workingStatus = vendingMachineStatus.workingStatus();
    jpaStatusReport.rfidStatus = vendingMachineStatus.rfidStatus();
    jpaStatusReport.smartCardStatus = vendingMachineStatus.smartCardStatus();
    jpaStatusReport.changeMoneyStatus = vendingMachineStatus.changeMoneyStatus();
    jpaStatusReport.vendingMachineSerialNumber = statusReport.serialNumber().value();

    return jpaStatusReport;
  }

  public VendingMachineStatusReport toDomain() {
    return new VendingMachineStatusReport(
        new VendingMachineStatusReportId(getId()),
        SerialNumber.of(vendingMachineSerialNumber),
        lastIntervention,
        new VendingMachineStatus(
            Temperature.of(measuredTemperature),
            powerStatus,
            workingStatus,
            rfidStatus,
            smartCardStatus,
            changeMoneyStatus),
        getCreatedAt());
  }
}
