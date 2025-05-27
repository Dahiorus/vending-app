package me.dahiorus.project.vending.fixture;

import me.dahiorus.project.vending.domain.machine.entity.Address;
import me.dahiorus.project.vending.domain.machine.entity.Address.City;
import me.dahiorus.project.vending.domain.machine.entity.Address.GeoCoordinates;
import me.dahiorus.project.vending.domain.machine.entity.Address.PostalCode;
import me.dahiorus.project.vending.domain.machine.entity.Address.StreetName;
import me.dahiorus.project.vending.domain.machine.entity.Address.StreetNumber;

public class AddressFixture {
  public static Builder anAddress() {
    return new Builder()
        .coordinates(1.23654789, 48.23654789)
        .streetNumber(4)
        .streetName("Bvd Davout")
        .postalCode("75020")
        .city("Paris");
  }

  public static class Builder {
    private GeoCoordinates coordinates;
    private Integer streetNumber;
    private String streetName;
    private String postalCode;
    private String city;

    public Builder coordinates(Double latitude, Double longitude) {
      this.coordinates = GeoCoordinates.of(latitude, longitude);
      return this;
    }

    public Builder streetNumber(Integer streetNumber) {
      this.streetNumber = streetNumber;
      return this;
    }

    public Builder streetName(String streetName) {
      this.streetName = streetName;
      return this;
    }

    public Builder postalCode(String postalCode) {
      this.postalCode = postalCode;
      return this;
    }

    public Builder city(String city) {
      this.city = city;
      return this;
    }

    public Address build() {
      return new Address(
          coordinates,
          StreetNumber.of(streetNumber),
          StreetName.of(streetName),
          PostalCode.of(postalCode),
          City.of(city));
    }
  }
}
