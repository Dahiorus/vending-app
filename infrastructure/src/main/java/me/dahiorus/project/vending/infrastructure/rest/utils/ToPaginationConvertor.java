package me.dahiorus.project.vending.infrastructure.rest.utils;

import static java.util.stream.Collectors.toSet;

import me.dahiorus.project.vending.domain.pagination.entity.PageNumber;
import me.dahiorus.project.vending.domain.pagination.entity.PageSize;
import me.dahiorus.project.vending.domain.pagination.entity.PageSort;
import me.dahiorus.project.vending.domain.pagination.entity.PageSort.SortProperty;
import me.dahiorus.project.vending.domain.pagination.entity.Pagination;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record ToPaginationConvertor(Pageable pageable) {

  public static Pagination toPagination(Pageable pageable) {
    return new ToPaginationConvertor(pageable).convert();
  }

  public Pagination convert() {
    return new Pagination(
        new PageNumber(pageable.getPageNumber()),
        new PageSize(pageable.getPageSize()),
        toPageSort(pageable.getSort()));
  }

  private static PageSort toPageSort(Sort sort) {
    return new PageSort(
        sort.stream()
            .map(order -> new SortProperty(order.getProperty(), toDirection(order.getDirection())))
            .collect(toSet()));
  }

  private static Pagination.Direction toDirection(Sort.Direction sortDirection) {
    return switch (sortDirection) {
      case ASC -> Pagination.Direction.ASC;
      case DESC -> Pagination.Direction.DESC;
    };
  }
}
