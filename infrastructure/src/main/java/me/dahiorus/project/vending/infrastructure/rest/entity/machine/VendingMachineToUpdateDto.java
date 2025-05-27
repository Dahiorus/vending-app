package me.dahiorus.project.vending.infrastructure.rest.entity.machine;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import me.dahiorus.project.vending.domain.item.entity.ItemType;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.CardSystemStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.ChangeSystemStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.PowerStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.Temperature;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.WorkingStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineToUpdate;

public record VendingMachineToUpdateDto(
    @NotNull AddressDto address,
    @NotNull Integer temperature,
    @NotNull ItemType itemType,
    @NotNull PowerStatus powerStatus,
    @NotNull WorkingStatus workingStatus,
    @NotNull CardSystemStatus rfidStatus,
    @NotNull CardSystemStatus smartCardStatus,
    @NotNull ChangeSystemStatus changeMoneyStatus,
    LocalDateTime lastIntervention) {

  public VendingMachineToUpdate toDomain(UUID id) {
    return new VendingMachineToUpdate(
        new VendingMachineId(id),
        address.toDomain(),
        new VendingMachineStatus(
            Temperature.of(temperature),
            powerStatus,
            workingStatus,
            rfidStatus,
            smartCardStatus,
            changeMoneyStatus),
        lastIntervention);
  }
}
