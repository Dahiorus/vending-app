package me.dahiorus.project.vending.domain.pagination.entity;

import static java.util.stream.Collectors.toSet;
import static me.dahiorus.project.vending.domain.pagination.entity.Pagination.Direction.ASC;
import static me.dahiorus.project.vending.domain.pagination.entity.Pagination.Direction.DESC;

import java.util.Set;
import me.dahiorus.project.vending.domain.pagination.entity.Pagination.Direction;

public record PageSort(Set<SortProperty> sortProperties) {
  public static PageSort asc(Set<String> properties) {
    return new PageSort(properties.stream().map(SortProperty::asc).collect(toSet()));
  }

  public static PageSort desc(Set<String> properties) {
    return new PageSort(properties.stream().map(SortProperty::desc).collect(toSet()));
  }

  public record SortProperty(String name, Direction direction) {
    public static SortProperty asc(String name) {
      return new SortProperty(name, ASC);
    }

    public static SortProperty desc(String name) {
      return new SortProperty(name, DESC);
    }
  }
}
