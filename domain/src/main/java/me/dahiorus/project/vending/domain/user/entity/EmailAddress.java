package me.dahiorus.project.vending.domain.user.entity;

import static me.dahiorus.project.vending.domain.utils.StringUtils.isBlank;

import java.io.Serializable;

public record EmailAddress(String value) implements Serializable {

  public EmailAddress {
    if (isBlank(value)) {
      throw new IllegalArgumentException("EmailAddress value cannot be blank");
    }
  }

  public static EmailAddress of(final String value) {
    return new EmailAddress(value);
  }
}
