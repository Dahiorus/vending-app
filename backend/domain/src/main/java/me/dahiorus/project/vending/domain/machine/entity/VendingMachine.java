package me.dahiorus.project.vending.domain.machine.entity;

import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.CardSystemStatus.OK;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.ChangeSystemStatus.NORMAL;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.PowerStatus.POWER_ON;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.WorkingStatus.WORKING;

import java.io.Serializable;
import java.time.LocalDateTime;
import me.dahiorus.project.vending.domain.item.entity.Item;
import me.dahiorus.project.vending.domain.item.entity.ItemType;

public record VendingMachine(
    VendingMachineId id,
    SerialNumber serialNumber,
    Address address,
    ItemType itemType,
    VendingMachineStatus status,
    LocalDateTime lastIntervention)
    implements Serializable {
  public VendingMachine markIntervention(LocalDateTime at) {
    return new VendingMachine(id, serialNumber, address, itemType, status, at);
  }

  public VendingMachine updateFrom(VendingMachineToUpdate toUpdate) {
    return new VendingMachine(
        id,
        serialNumber,
        toUpdate.address(),
        itemType,
        toUpdate.status(),
        toUpdate.lastIntervention());
  }

  public VendingMachineToUpdate toUpdate() {
    return new VendingMachineToUpdate(id, address, status, lastIntervention);
  }

  public boolean supports(Item item) {
    return itemType == item.type();
  }

  public boolean isAllSystemClear() {
    return status.isAllSystemClear();
  }

  public VendingMachine resetAllStatuses() {
    var resetStatus =
        new VendingMachineStatus(status.temperature(), POWER_ON, WORKING, OK, OK, NORMAL);
    return new VendingMachine(id, serialNumber, address, itemType, resetStatus, lastIntervention);
  }

  public boolean isWorking() {
    return status.isWorking();
  }
}
