package me.dahiorus.project.vending.domain.user.entity;

import java.io.Serializable;

public record Password(String value) implements Serializable {

  public static Password of(final String value) {
    return new Password(value);
  }

  public boolean isEmpty() {
    return value == null || value.isEmpty();
  }

  public int length() {
    return value.length();
  }

  @Override
  public String toString() {
    return value == null ? "Password[]" : "Password[******]";
  }
}
