package me.dahiorus.project.vending.infrastructure.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Optional;
import me.dahiorus.project.vending.domain.machine.entity.Address;
import me.dahiorus.project.vending.domain.machine.entity.Address.City;
import me.dahiorus.project.vending.domain.machine.entity.Address.GeoCoordinates;
import me.dahiorus.project.vending.domain.machine.entity.Address.PostalCode;
import me.dahiorus.project.vending.domain.machine.entity.Address.StreetName;
import me.dahiorus.project.vending.domain.machine.entity.Address.StreetNumber;

@Embeddable
public class JpaAddress {
  @Column(precision = 7)
  private Double latitude;

  @Column(precision = 7)
  private Double longitude;

  @Column(nullable = false)
  private Integer streetNumber;

  @Column(nullable = false)
  private String streetName;

  @Column(nullable = false)
  private String postalCode;

  @Column(nullable = false)
  private String city;

  public static JpaAddress fromDomain(Address address) {
    JpaAddress jpaAddress = new JpaAddress();
    jpaAddress.latitude =
        Optional.ofNullable(address.coordinates()).map(GeoCoordinates::latitude).orElse(null);
    jpaAddress.longitude =
        Optional.ofNullable(address.coordinates()).map(GeoCoordinates::longitude).orElse(null);
    jpaAddress.streetNumber =
        Optional.ofNullable(address.streetNumber()).map(StreetNumber::value).orElse(null);
    jpaAddress.streetName =
        Optional.ofNullable(address.streetName()).map(StreetName::value).orElse(null);
    jpaAddress.postalCode =
        Optional.ofNullable(address.postalCode()).map(PostalCode::value).orElse(null);
    jpaAddress.city = Optional.ofNullable(address.city()).map(City::value).orElse(null);

    return jpaAddress;
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
