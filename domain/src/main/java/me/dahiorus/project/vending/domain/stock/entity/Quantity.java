package me.dahiorus.project.vending.domain.stock.entity;

import java.io.Serializable;

public record Quantity(int value) implements Serializable {
  public Quantity {
    if (value < 0) {
      throw new IllegalArgumentException("Quantity cannot be negative");
    }
  }

  public static Quantity empty() {
    return new Quantity(0);
  }

  public static Quantity of(final Integer value) {
    return new Quantity(value);
  }

  public Quantity add(Quantity amount) {
    return new Quantity(value + amount.value);
  }

  public Quantity decrement() {
    return new Quantity(value - 1);
  }
}
