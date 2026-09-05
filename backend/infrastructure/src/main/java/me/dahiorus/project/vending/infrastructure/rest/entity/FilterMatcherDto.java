package me.dahiorus.project.vending.infrastructure.rest.entity;

import static java.util.Objects.requireNonNullElse;

import io.swagger.v3.oas.annotations.media.Schema;
import me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher;
import me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.CaseSensitivity;
import me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.IgnoreOrIncludeNull;
import me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.MatchAllOrAny;
import me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.StringMatch;

public record FilterMatcherDto(
    @Schema(
            example = "CONTAINING",
            description = "Match modes for treatment of String values",
            implementation = StringMatch.class)
        StringMatch stringMatch,
    @Schema(
            example = "ALL",
            description = "How to concatenate all matching properties",
            implementation = MatchAllOrAny.class)
        MatchAllOrAny matchAllOrAny,
    @Schema(
            example = "IGNORE",
            description = "How to handle null properties",
            implementation = IgnoreOrIncludeNull.class)
        IgnoreOrIncludeNull ignoreOrIncludeNull,
    @Schema(
            example = "CASE_INSENSITIVE",
            description = "Searching ignoring case",
            implementation = CaseSensitivity.class)
        CaseSensitivity caseSensitivity) {

  public FilterMatcher toDomain() {
    var defaults = new FilterMatcher();

    return new FilterMatcher(
        requireNonNullElse(stringMatch, defaults.stringMatch()),
        requireNonNullElse(matchAllOrAny, defaults.matchAllOrAny()),
        requireNonNullElse(ignoreOrIncludeNull, defaults.ignoreOrIncludeNull()),
        requireNonNullElse(caseSensitivity, defaults.caseSensitivity()));
  }
}

