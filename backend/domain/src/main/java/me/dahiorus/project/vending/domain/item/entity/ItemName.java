package me.dahiorus.project.vending.domain.item.entity;

import static java.util.Objects.requireNonNullElse;

import java.io.Serializable;

public record ItemName(String value) implements Serializable {
  public ItemName {
    if (requireNonNullElse(value, "").isBlank()) {
      throw new IllegalArgumentException("Item name must not be null or blank");
    }
  }

  public static ItemName of(String value) {
    return new ItemName(value);
  }
}
