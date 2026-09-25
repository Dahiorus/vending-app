package me.dahiorus.project.vending.infrastructure.rest.entity.machine;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.dahiorus.project.vending.domain.machine.entity.Address;
import me.dahiorus.project.vending.domain.machine.entity.Address.City;
import me.dahiorus.project.vending.domain.machine.entity.Address.GeoCoordinates;
import me.dahiorus.project.vending.domain.machine.entity.Address.PostalCode;
import me.dahiorus.project.vending.domain.machine.entity.Address.StreetName;
import me.dahiorus.project.vending.domain.machine.entity.Address.StreetNumber;

public record AddressDto(
    @NotNull Double latitude,
    @NotNull Double longitude,
    @NotNull Integer streetNumber,
    @NotBlank String streetName,
    @NotBlank String postalCode,
    @NotBlank String city) {
  public static AddressDto fromDomain(Address address) {
    return new AddressDto(
        address.coordinates().latitude(),
        address.coordinates().longitude(),
        address.streetNumber().value(),
        address.streetName().value(),
        address.postalCode().value(),
        address.city().value());
  }

  public Address toDomain() {
    return new Address(
        GeoCoordinates.of(latitude, longitude),
        StreetNumber.of(streetNumber),
        StreetName.of(streetName),
        PostalCode.of(postalCode),
        City.of(city));
  }
}
