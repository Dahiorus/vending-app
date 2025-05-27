package me.dahiorus.project.vending.domain.pagination.entity;

public record PageSize(int value) {
  public PageSize {
    if (value <= 0) {
      throw new IllegalArgumentException("Page size must be greater than zero");
    }
  }
}
