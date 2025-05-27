package me.dahiorus.project.vending.domain.user.entity;

import java.io.Serializable;

public record Firstname(String value) implements Serializable {

  public static Firstname of(final String value) {
    return new Firstname(value);
  }
}
