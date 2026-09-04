package me.dahiorus.project.vending.domain.pagination.entity;

import static java.util.stream.Collectors.toSet;

import java.util.Map;
import java.util.Set;
import me.dahiorus.project.vending.domain.pagination.entity.PageSort.SortProperty;

public record Pagination(PageNumber number, PageSize size, PageSort sort) {
  public Pagination(int number, int size, Map<Direction, Set<String>> sortProperties) {
    this(
        new PageNumber(number),
        new PageSize(size),
        new PageSort(
            sortProperties.entrySet().stream()
                .map(entry -> toSortProperties(entry.getKey(), entry.getValue()))
                .flatMap(Set::stream)
                .collect(toSet())));
  }

  private static Set<SortProperty> toSortProperties(Direction direction, Set<String> properties) {
    return properties.stream().map(prop -> new SortProperty(prop, direction)).collect(toSet());
  }

  public Pagination() {
    this(new PageNumber(0), new PageSize(20), new PageSort(Set.of()));
  }

  public int pageNumber() {
    return number.value();
  }

  public int pageSize() {
    return size.value();
  }

  public enum Direction {
    ASC,
    DESC
  }
}
