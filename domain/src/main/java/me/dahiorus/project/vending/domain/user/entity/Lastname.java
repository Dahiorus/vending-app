package me.dahiorus.project.vending.domain.user.entity;

import java.io.Serializable;

public record Lastname(String value) implements Serializable {

  public static Lastname of(final String value) {
    return new Lastname(value);
  }
}
