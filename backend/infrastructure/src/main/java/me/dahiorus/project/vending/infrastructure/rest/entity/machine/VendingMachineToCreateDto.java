package me.dahiorus.project.vending.infrastructure.rest.entity.machine;

import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.defaultStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.dahiorus.project.vending.domain.item.entity.ItemType;
import me.dahiorus.project.vending.domain.machine.entity.Address;
import me.dahiorus.project.vending.domain.machine.entity.Address.City;
import me.dahiorus.project.vending.domain.machine.entity.Address.GeoCoordinates;
import me.dahiorus.project.vending.domain.machine.entity.Address.PostalCode;
import me.dahiorus.project.vending.domain.machine.entity.Address.StreetName;
import me.dahiorus.project.vending.domain.machine.entity.Address.StreetNumber;
import me.dahiorus.project.vending.domain.machine.entity.SerialNumber;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;

public record VendingMachineToCreateDto(
    @NotBlank String serialNumber,
    @NotNull AddressToCreateDto address,
    @NotNull ItemType itemType) {
  public VendingMachine toDomain() {
    return new VendingMachine(
        null, SerialNumber.of(serialNumber), address.toDomain(), itemType, defaultStatus(), null);
  }

  public record AddressToCreateDto(
      @NotNull Double latitude,
      @NotNull Double longitude,
      @NotNull Integer streetNumber,
      @NotBlank String streetName,
      @NotBlank String postalCode,
      @NotBlank String city) {

    public Address toDomain() {
      return new Address(
          GeoCoordinates.of(latitude, longitude),
          StreetNumber.of(streetNumber),
          StreetName.of(streetName),
          PostalCode.of(postalCode),
          City.of(city));
    }
  }
}
