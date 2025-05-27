package me.dahiorus.project.vending.domain.pagination.entity;

public record PageNumber(int value) {
  public PageNumber {
    if (value < 0) {
      throw new IllegalArgumentException("Page number cannot be negative");
    }
  }
}
