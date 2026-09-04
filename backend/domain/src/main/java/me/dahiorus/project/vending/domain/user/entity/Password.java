package me.dahiorus.project.vending.domain.user.entity;

public record Password(String value) {

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
