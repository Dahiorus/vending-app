package me.dahiorus.project.vending.infrastructure.jpa.repository;

import me.dahiorus.project.vending.domain.pagination.entity.PageSort;
import me.dahiorus.project.vending.domain.pagination.entity.Pagination;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record ToPageableConvertor(Pagination pagination) {
  public static Pageable toPageable(Pagination pagination) {
    return new ToPageableConvertor(pagination).convert();
  }

  private Pageable convert() {
    return PageRequest.of(
        pagination.pageNumber(), pagination.pageSize(), toSort(pagination.sort()));
  }

  private static Sort toSort(PageSort pageSort) {
    return Sort.by(
        pageSort.sortProperties().stream()
            .map(
                sortProperty ->
                    new Sort.Order(toDirection(sortProperty.direction()), sortProperty.name()))
            .toList());
  }

  private static Sort.Direction toDirection(Pagination.Direction direction) {
    return switch (direction) {
      case ASC -> Sort.Direction.ASC;
      case DESC -> Sort.Direction.DESC;
    };
  }
}
