package me.dahiorus.project.vending.domain.item.entity;

import static me.dahiorus.project.vending.domain.utils.StringUtils.isBlank;

import java.io.Serializable;

public record ItemName(String value) implements Serializable {
  public ItemName {
    if (isBlank(value)) {
      throw new IllegalArgumentException("Item name must not be null or blank");
    }
  }

  public static ItemName of(String value) {
    return new ItemName(value);
  }
}
