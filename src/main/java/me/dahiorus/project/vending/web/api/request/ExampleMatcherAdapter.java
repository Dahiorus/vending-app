package me.dahiorus.project.vending.web.api.request;

import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.MatchMode;
import org.springframework.data.domain.ExampleMatcher.NullHandler;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class ExampleMatcherAdapter
{
  @Schema(defaultValue = "CONTAINING", description = "Match modes for treatment of String values")
  @Getter
  @Setter
  private StringMatcher stringMatcher = StringMatcher.CONTAINING;

  @Schema(defaultValue = "ALL", description = "How to concatenate all matching properties")
  @Getter
  @Setter
  private MatchMode matchMode = MatchMode.ALL;

  @Schema(defaultValue = "IGNORE", description = "How to handle null properties")
  @Getter
  @Setter
  private NullHandler nullHandler = NullHandler.IGNORE;

  @Schema(defaultValue = "true", description = "Searching ignoring case")
  @Getter
  @Setter
  private boolean ignoreCase = true;

  public ExampleMatcher get()
  {
    ExampleMatcher matcher = matchMode == MatchMode.ANY ? ExampleMatcher.matchingAny() : ExampleMatcher.matchingAll();

    return matcher.withNullHandler(nullHandler)
      .withIgnoreCase(ignoreCase)
      .withStringMatcher(stringMatcher);
  }
}
