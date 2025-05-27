package me.dahiorus.project.vending.domain.machine.entity;

import static me.dahiorus.project.vending.domain.utils.StringUtils.isBlank;

import java.io.Serializable;

public record SerialNumber(String value) implements Serializable {
  public SerialNumber {
    if (isBlank(value)) {
      throw new IllegalArgumentException("Serial number cannot be null or blank");
    }
  }

  public static SerialNumber of(final String value) {
    return new SerialNumber(value);
  }
}
