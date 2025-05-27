package me.dahiorus.project.vending.domain.pagination.entity;

import static java.util.Collections.unmodifiableList;

import java.util.List;
import java.util.function.Function;

public record PageResult<D>(List<D> content, Pagination pagination, Total total) {
  public PageResult {
    content = unmodifiableList(content);
  }

  public <S> PageResult<S> map(Function<D, S> mapper) {
    return new PageResult<>(content.stream().map(mapper).toList(), pagination, total);
  }

  public long totalElements() {
    return total.value();
  }
}
