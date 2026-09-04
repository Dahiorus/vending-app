package me.dahiorus.project.vending.domain.file.entity;

import java.io.Serializable;
import java.util.Arrays;

public record BinaryContent(byte[] value) implements Serializable {
  public BinaryContent {
    if (value == null || value.length == 0) {
      throw new IllegalArgumentException("Binary content cannot be null or empty");
    }
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof BinaryContent(byte[] otherValue))) {
      return false;
    }

    return Arrays.equals(this.value, otherValue);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(value);
  }

  @Override
  public String toString() {
    return Arrays.toString(value);
  }
}
