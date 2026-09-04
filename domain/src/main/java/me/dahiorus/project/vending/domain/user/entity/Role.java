package me.dahiorus.project.vending.domain.user.entity;

import static java.lang.String.format;
import static java.util.Objects.requireNonNullElse;

import java.io.Serializable;

public record Role(String value) implements Serializable {
  public Role {
    if (requireNonNullElse(value, "").isBlank()) {
      throw new IllegalArgumentException("Role value cannot be blank");
    }
  }

  public String asRole() {
    return format("ROLE_%s".toUpperCase(), value);
  }
}
