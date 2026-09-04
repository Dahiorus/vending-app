package me.dahiorus.project.vending.infrastructure.jpa.repository;

import static me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.CaseSensitivity.CASE_INSENSITIVE;
import static me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.IgnoreOrIncludeNull.IGNORE;
import static me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.MatchAllOrAny.ANY;
import static org.springframework.data.domain.ExampleMatcher.matchingAll;
import static org.springframework.data.domain.ExampleMatcher.matchingAny;

import java.util.function.Function;
import me.dahiorus.project.vending.domain.pagination.entity.Filter;
import me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher;
import me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.CaseSensitivity;
import me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.IgnoreOrIncludeNull;
import me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.StringMatch;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaEntity;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.NullHandler;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;

public class ExampleMatcherAdapter {
  private ExampleMatcherAdapter() {}

  public static <S, E extends JpaEntity> Example<E> toExample(
      Filter<S> filter, Function<S, E> entityMapper) {
    return Example.of(entityMapper.apply(filter.probe()), toExampleMatcher(filter.filterMatcher()));
  }

  public static ExampleMatcher toExampleMatcher(FilterMatcher filterMatcher) {
    var exampleMatcher = ANY == filterMatcher.matchAllOrAny() ? matchingAny() : matchingAll();

    return exampleMatcher
        .withNullHandler(nullHandler(filterMatcher.ignoreOrIncludeNull()))
        .withIgnoreCase(ignoreCase(filterMatcher.caseSensitivity()))
        .withStringMatcher(stringMatcher(filterMatcher.stringMatch()));
  }

  private static boolean ignoreCase(final CaseSensitivity caseSensitivity) {
    return caseSensitivity == CASE_INSENSITIVE;
  }

  private static NullHandler nullHandler(IgnoreOrIncludeNull ignoreOrIncludeNull) {
    return IGNORE == ignoreOrIncludeNull ? NullHandler.IGNORE : NullHandler.INCLUDE;
  }

  private static StringMatcher stringMatcher(StringMatch stringMatch) {
    return stringMatch == null
        ? StringMatcher.DEFAULT
        : switch (stringMatch) {
          case EXACT -> StringMatcher.EXACT;
          case STARTING -> StringMatcher.STARTING;
          case ENDING -> StringMatcher.ENDING;
          case CONTAINING -> StringMatcher.CONTAINING;
          case REGEX -> StringMatcher.REGEX;
        };
  }
}
