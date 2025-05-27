package me.dahiorus.project.vending.domain.file.entity;

import static me.dahiorus.project.vending.domain.utils.StringUtils.isBlank;

import java.io.Serializable;

public record Filename(String value) implements Serializable {
  public Filename {
    if (isBlank(value)) {
      throw new IllegalArgumentException("Filename cannot be null or blank");
    }
    if (value.length() > 255) {
      throw new IllegalArgumentException("Filename cannot exceed 255 characters");
    }
  }
}
