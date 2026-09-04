package me.dahiorus.project.vending.domain.user.entity;

import static java.util.Objects.requireNonNullElse;

import java.io.Serializable;

public record EmailAddress(String value) implements Serializable {

  public EmailAddress {
    if (requireNonNullElse(value, "").isBlank()) {
      throw new IllegalArgumentException("EmailAddress value cannot be blank");
    }
  }

  public static EmailAddress of(final String value) {
    return new EmailAddress(value);
  }
}
