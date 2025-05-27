package me.dahiorus.project.vending.fixture;

import static java.time.LocalDateTime.now;
import static me.dahiorus.project.vending.domain.item.entity.ItemType.SNACK;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.PowerStatus.POWER_ON;
import static me.dahiorus.project.vending.fixture.AddressFixture.anAddress;
import static me.dahiorus.project.vending.fixture.VendingMachineStatusFixture.aVendingMachineStatus;

import java.time.LocalDateTime;
import java.util.UUID;
import me.dahiorus.project.vending.domain.item.entity.ItemType;
import me.dahiorus.project.vending.domain.machine.entity.Address;
import me.dahiorus.project.vending.domain.machine.entity.SerialNumber;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.WorkingStatus;

public class VendingMachineFixture {
  public static Builder aVendingMachine() {
    return new Builder()
        .id(new VendingMachineId(UUID.randomUUID()))
        .serialNumber("VM-123456")
        .address(anAddress().build())
        .itemType(SNACK)
        .status(aVendingMachineStatus().build())
        .lastIntervention(now());
  }

  public static Builder aVendingMachineWithWorkingStatus(WorkingStatus workingStatus) {
    return aVendingMachine()
        .status(aVendingMachineStatus().powerStatus(POWER_ON).workingStatus(workingStatus).build());
  }

  public static class Builder {
    private VendingMachineId id;
    private String serialNumber;
    private Address address;
    private ItemType itemType;
    private VendingMachineStatus status;
    private LocalDateTime lastIntervention;

    public Builder id(VendingMachineId id) {
      this.id = id;
      return this;
    }

    public Builder serialNumber(String serialNumber) {
      this.serialNumber = serialNumber;
      return this;
    }

    public Builder address(Address address) {
      this.address = address;
      return this;
    }

    public Builder itemType(ItemType itemType) {
      this.itemType = itemType;
      return this;
    }

    public Builder status(VendingMachineStatus status) {
      this.status = status;
      return this;
    }

    public Builder lastIntervention(LocalDateTime lastIntervention) {
      this.lastIntervention = lastIntervention;
      return this;
    }

    public VendingMachine build() {
      return new VendingMachine(
          id, SerialNumber.of(serialNumber), address, itemType, status, lastIntervention);
    }
  }
}
