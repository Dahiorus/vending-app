package me.dahiorus.project.vending.infrastructure.rest.entity.machine;

import me.dahiorus.project.vending.domain.machine.entity.Address;
import me.dahiorus.project.vending.domain.machine.entity.Address.City;
import me.dahiorus.project.vending.domain.machine.entity.Address.GeoCoordinates;
import me.dahiorus.project.vending.domain.machine.entity.Address.PostalCode;
import me.dahiorus.project.vending.domain.machine.entity.Address.StreetName;
import me.dahiorus.project.vending.domain.machine.entity.Address.StreetNumber;

public record AddressDto(
    Double latitude,
    Double longitude,
    Integer streetNumber,
    String streetName,
    String postalCode,
    String city) {
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
