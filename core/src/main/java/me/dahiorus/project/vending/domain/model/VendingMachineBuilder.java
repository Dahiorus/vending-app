package me.dahiorus.project.vending.domain.model;

import java.time.Instant;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.dahiorus.project.vending.domain.model.dto.VendingMachineDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VendingMachineBuilder
{
  private VendingMachine machine = new VendingMachine();

  private VendingMachineDto dto = new VendingMachineDto();

  public static VendingMachineBuilder builder()
  {
    return new VendingMachineBuilder();
  }

  public VendingMachineBuilder itemType(final ItemType type)
  {
    machine.setType(type);
    dto.setType(type);
    return this;
  }

  public VendingMachineBuilder id(final UUID id)
  {
    machine.setId(id);
    dto.setId(id);
    return this;
  }

  public VendingMachineBuilder powerStatus(final PowerStatus powerStatus)
  {
    machine.setPowerStatus(powerStatus);
    dto.setPowerStatus(powerStatus);
    return this;
  }

  public VendingMachineBuilder workingStatus(final WorkingStatus workingStatus)
  {
    machine.setWorkingStatus(workingStatus);
    dto.setWorkingStatus(workingStatus);
    return this;
  }

  public VendingMachineBuilder rfidStatus(final CardSystemStatus rfidStatus)
  {
    machine.setRfidStatus(rfidStatus);
    dto.setRfidStatus(rfidStatus);
    return this;
  }

  public VendingMachineBuilder smartCardStatus(
    final CardSystemStatus smartCardStatus)
  {
    machine.setSmartCardStatus(smartCardStatus);
    dto.setSmartCardStatus(smartCardStatus);
    return this;
  }

  public VendingMachineBuilder changeMoneyStatus(
    final ChangeSystemStatus changeMoneyStatus)
  {
    machine.setChangeMoneyStatus(changeMoneyStatus);
    dto.setChangeMoneyStatus(changeMoneyStatus);
    return this;
  }

  public VendingMachineBuilder serialNumber(final String serialNumber)
  {
    machine.setSerialNumber(serialNumber);
    dto.setSerialNumber(serialNumber);
    return this;
  }

  public VendingMachineBuilder latitude(final Double latitude)
  {
    machine.getAddress()
      .setLatitude(latitude);
    dto.getAddress().setLatitude(latitude);
    return this;
  }

  public VendingMachineBuilder longitude(final Double longitude)
  {
    machine.getAddress()
      .setLongitude(longitude);
    dto.getAddress().setLongitude(longitude);
    return this;
  }

  public VendingMachineBuilder place(final String place)
  {
    machine.getAddress()
      .setPlace(place);
    dto.getAddress()
      .setPlace(place);
    return this;
  }

  public VendingMachineBuilder address(final String address)
  {
    machine.getAddress()
      .setStreetAddress(address);
    dto.getAddress()
      .setStreetAddress(address);
    return this;
  }

  public VendingMachineBuilder lastIntervention(final Instant lastIntervention)
  {
    machine.setLastIntervention(lastIntervention);
    dto.setLastIntervention(lastIntervention);
    return this;
  }

  public VendingMachineBuilder temperature(final Integer temperature)
  {
    machine.setTemperature(temperature);
    dto.setTemperature(temperature);
    return this;
  }

  public VendingMachine build()
  {
    return machine;
  }

  public VendingMachineDto buildDto()
  {
    return dto;
  }
}
