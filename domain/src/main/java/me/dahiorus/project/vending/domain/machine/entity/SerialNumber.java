package me.dahiorus.project.vending.domain.machine.entity;

import static java.util.Objects.requireNonNullElse;

import java.io.Serializable;

public record SerialNumber(String value) implements Serializable {
  public SerialNumber {
    if (requireNonNullElse(value, "").isBlank()) {
      throw new IllegalArgumentException("Serial number cannot be null or blank");
    }
  }

  public static SerialNumber of(final String value) {
    return new SerialNumber(value);
  }
}
