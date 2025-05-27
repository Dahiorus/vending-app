package me.dahiorus.project.vending.infrastructure.rest.entity.machine;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.UUID;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.CardSystemStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.ChangeSystemStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.PowerStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.WorkingStatus;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStatusReport;

public record VendingMachineStatusReportDto(
    @JsonIgnore UUID vendingMachineId,
    String serialNumber,
    LocalDateTime lastIntervention,
    Integer temperature,
    PowerStatus powerStatus,
    WorkingStatus workingStatus,
    CardSystemStatus rfidStatus,
    CardSystemStatus smartCardStatus,
    ChangeSystemStatus changeMoneyStatus) {

  public static VendingMachineStatusReportDto fromDomain(
      VendingMachineId id, VendingMachineStatusReport statusReport) {
    return new VendingMachineStatusReportDto(
        id.value(),
        statusReport.serialNumber().value(),
        statusReport.lastIntervention(),
        statusReport.status().temperature().value(),
        statusReport.status().powerStatus(),
        statusReport.status().workingStatus(),
        statusReport.status().rfidStatus(),
        statusReport.status().smartCardStatus(),
        statusReport.status().changeMoneyStatus());
  }
}
