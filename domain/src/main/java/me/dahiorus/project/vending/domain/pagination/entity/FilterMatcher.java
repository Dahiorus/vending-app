package me.dahiorus.project.vending.domain.pagination.entity;

import static me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.CaseSensitivity.CASE_INSENSITIVE;
import static me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.IgnoreOrIncludeNull.IGNORE;
import static me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.MatchAllOrAny.ALL;
import static me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.StringMatch.CONTAINING;

public record FilterMatcher(
    StringMatch stringMatch,
    MatchAllOrAny matchAllOrAny,
    IgnoreOrIncludeNull ignoreOrIncludeNull,
    CaseSensitivity caseSensitivity) {

  public FilterMatcher() {
    this(CONTAINING, ALL, IGNORE, CASE_INSENSITIVE);
  }

  public enum StringMatch {
    EXACT,
    STARTING,
    ENDING,
    CONTAINING,
    REGEX
  }

  public enum MatchAllOrAny {
    ALL,
    ANY
  }

  public enum IgnoreOrIncludeNull {
    INCLUDE,
    IGNORE;
  }

  public enum CaseSensitivity {
    CASE_INSENSITIVE,
    CASE_SENSITIVE
  }
}
