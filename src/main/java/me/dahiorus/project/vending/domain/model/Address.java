package me.dahiorus.project.vending.domain.model;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Address
{
  private Double latitude;

  private Double longitude;

  private String place;

  private String streetAddress;

  @Column(precision = 7)
  public Double getLatitude()
  {
    return latitude;
  }

  public void setLatitude(final Double latitude)
  {
    this.latitude = latitude;
  }

  @Column(precision = 7)
  public Double getLongitude()
  {
    return longitude;
  }

  public void setLongitude(final Double longitude)
  {
    this.longitude = longitude;
  }

  @Column
  public String getPlace()
  {
    return place;
  }

  public void setPlace(final String place)
  {
    this.place = place;
  }

  @Column(nullable = false)
  public String getStreetAddress()
  {
    return streetAddress;
  }

  public void setStreetAddress(final String streetAddress)
  {
    this.streetAddress = streetAddress;
  }

  @Override
  public String toString()
  {
    return "Address [latitude=" + latitude + ", longitude=" + longitude + ", place=" + place + ", streetAddress="
      + streetAddress + "]";
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(latitude, longitude, place, streetAddress);
  }

  @Override
  public boolean equals(final Object obj)
  {
    if (this == obj)
    {
      return true;
    }
    if (!(obj instanceof Address))
    {
      return false;
    }
    Address other = (Address) obj;
    return Objects.equals(latitude, other.latitude) && Objects.equals(longitude, other.longitude)
      && Objects.equals(place, other.place) && Objects.equals(streetAddress, other.streetAddress);
  }
}
