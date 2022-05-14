package me.dahiorus.project.vending.core.service.validation;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString(of = { "code", "defaultMessage" })
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ValidationError implements Serializable
{
  private static final long serialVersionUID = -8799197003814321507L;

  private static final String COMMON_PREFIX = "validation.constraints.";

  @Getter
  private final String code;

  @Getter
  private final String defaultMessage;

  @Getter
  private final Object[] errorArgs;

  public static ValidationError objectError(final String code, final String defaultMessage, final Object... errorArgs)
  {
    return new ValidationError(code, defaultMessage, errorArgs);
  }

  public static String getFullCode(final String suffix)
  {
    return COMMON_PREFIX + suffix;
  }

  enum CommonError
  {
    EMPTY_VALUE("empty_value"),
    NOT_UNIQUE("not_unique"),
    MAX_LENGTH("max_length"),
    ;

    final String code;

    CommonError(final String code)
    {
      this.code = getFullCode(code);
    }
  }
}
