package me.dahiorus.project.vending.domain.machine.entity;

import static me.dahiorus.project.vending.domain.utils.StringUtils.isBlank;

import java.io.Serializable;

public record Address(
    GeoCoordinates coordinates,
    StreetNumber streetNumber,
    StreetName streetName,
    PostalCode postalCode,
    City city)
    implements Serializable {

  public record GeoCoordinates(Double latitude, Double longitude) {
    public GeoCoordinates {
      if (latitude < -90 || latitude > 90) {
        throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees.");
      }
      if (longitude < -180 || longitude > 180) {
        throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees.");
      }
    }

    public static GeoCoordinates of(Double latitude, Double longitude) {
      return new GeoCoordinates(latitude, longitude);
    }
  }

  public record City(String value) implements Serializable {
    public City {
      if (isBlank(value)) {
        throw new IllegalArgumentException("City name cannot be null or blank.");
      }
    }

    public static City of(String value) {
      return new City(value);
    }
  }

  public record PostalCode(String value) implements Serializable {
    public PostalCode {
      if (isBlank(value)) {
        throw new IllegalArgumentException("Postal code cannot be null or blank.");
      }
    }

    public static PostalCode of(String value) {
      return new PostalCode(value);
    }
  }

  public record StreetNumber(Integer value) implements Serializable {
    public StreetNumber {
      if (value == null || value <= 0) {
        throw new IllegalArgumentException("Street number must be a positive integer.");
      }
    }

    public static StreetNumber of(Integer value) {
      return new StreetNumber(value);
    }
  }

  public record StreetName(String value) implements Serializable {
    public StreetName {
      if (isBlank(value)) {
        throw new IllegalArgumentException("Street name cannot be null or blank.");
      }
    }

    public static StreetName of(String value) {
      return new StreetName(value);
    }
  }
}
