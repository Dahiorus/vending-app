package me.dahiorus.project.vending.infrastructure.rest.entity.machine;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
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
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "elements")
public record VendingMachineDto(
    @Parameter(hidden = true) @Schema(accessMode = READ_ONLY) UUID id,
    String serialNumber,
    AddressDto address,
    LocalDateTime lastIntervention,
    Integer temperature,
    ItemType itemType,
    PowerStatus powerStatus,
    WorkingStatus workingStatus,
    CardSystemStatus rfidStatus,
    CardSystemStatus smartCardStatus,
    ChangeSystemStatus changeMoneyStatus) {

  public VendingMachine toDomain() {
    return new VendingMachine(
        new VendingMachineId(id),
        Optional.ofNullable(serialNumber).map(SerialNumber::of).orElse(null),
        Optional.ofNullable(address).map(AddressDto::toDomain).orElse(null),
        itemType,
        new VendingMachineStatus(
            Optional.ofNullable(temperature).map(Temperature::of).orElse(null),
            powerStatus,
            workingStatus,
            rfidStatus,
            smartCardStatus,
            changeMoneyStatus),
        lastIntervention);
  }

  public static VendingMachineDto fromDomain(VendingMachine machine) {
    var status = machine.status();

    return new VendingMachineDto(
        machine.id().value(),
        machine.serialNumber().value(),
        AddressDto.fromDomain(machine.address()),
        machine.lastIntervention(),
        status.temperature().value(),
        machine.itemType(),
        status.powerStatus(),
        status.workingStatus(),
        status.rfidStatus(),
        status.smartCardStatus(),
        status.changeMoneyStatus());
  }
}
