package me.dahiorus.project.vending.domain;

import java.util.List;
import me.dahiorus.project.vending.domain.pagination.entity.Filter;
import me.dahiorus.project.vending.domain.pagination.entity.Pagination;

public interface SearchSpi<D, S> {
  List<D> search(Pagination pagination, Filter<S> filter);

  long count(Filter<S> filter);
}
