package me.dahiorus.project.vending.domain.file.entity;

import java.util.Optional;
import java.util.stream.Stream;

public enum ContentType {
  JPG("image/jpeg"),
  PNG("image/png");

  private final String type;

  ContentType(String type) {
    this.type = type;
  }

  public String value() {
    return type;
  }

  public static Optional<ContentType> of(String type) {
    return Stream.of(values())
        .filter(contentType -> contentType.value().equalsIgnoreCase(type))
        .findFirst();
  }
}
