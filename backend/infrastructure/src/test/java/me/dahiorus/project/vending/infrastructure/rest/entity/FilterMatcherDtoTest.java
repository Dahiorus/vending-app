package me.dahiorus.project.vending.infrastructure.rest.entity;

import static me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.CaseSensitivity.CASE_INSENSITIVE;
import static me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.CaseSensitivity.CASE_SENSITIVE;
import static me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.IgnoreOrIncludeNull.IGNORE;
import static me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.IgnoreOrIncludeNull.INCLUDE;
import static me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.MatchAllOrAny.ALL;
import static me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.MatchAllOrAny.ANY;
import static me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.StringMatch.CONTAINING;
import static me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.StringMatch.EXACT;
import static org.assertj.core.api.Assertions.assertThat;

import me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher;
import org.junit.jupiter.api.Test;

class FilterMatcherDtoTest {

  /**
   * A search request omitting the filter-matcher query params (stringMatch,
   * matchAllOrAny, ignoreOrIncludeNull, caseSensitivity) must behave like the plain
   * `GET /vending-machines`/`GET /items` search: ignoreOrIncludeNull must fall back to
   * IGNORE, not null/INCLUDE, otherwise ExampleMatcherAdapter matches only all-null
   * probes and every existing row is silently excluded from the results.
   */
  @Test
  void should_fall_back_to_FilterMatcher_defaults_when_all_params_are_missing() {
    var dto = new FilterMatcherDto(null, null, null, null);

    assertThat(dto.toDomain()).isEqualTo(new FilterMatcher());
  }

  @Test
  void should_keep_explicit_values_and_default_the_missing_ones() {
    var dto = new FilterMatcherDto(EXACT, null, INCLUDE, null);

    assertThat(dto.toDomain()).isEqualTo(new FilterMatcher(EXACT, ALL, INCLUDE, CASE_INSENSITIVE));
  }

  @Test
  void should_keep_all_explicit_values() {
    var dto = new FilterMatcherDto(CONTAINING, ANY, IGNORE, CASE_SENSITIVE);

    assertThat(dto.toDomain())
        .isEqualTo(new FilterMatcher(CONTAINING, ANY, IGNORE, CASE_SENSITIVE));
  }
}
