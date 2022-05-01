package me.dahiorus.project.vending.core.service.validation;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString(of = { "code", "defaultMessage" })
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ValidationError implements Serializable
{
  private static final long serialVersionUID = -8799197003814321507L;

  private static final String COMMON_PREFIX = "validation.constraints.";

  @Getter
  private String code;

  @Getter
  private String defaultMessage;

  @Getter
  private Object[] errorArgs;

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
    EMPTY_VALUE("empty_value", "is mandatory"),
    NOT_UNIQUE("not_unique", "must be unique");

    final String code;

    final String defaultMessage;

    CommonError(final String code, final String defaultMessage)
    {
      this.code = getFullCode(code);
      this.defaultMessage = defaultMessage;
    }

    String getDefaultMessageFor(final String field)
    {
      return field + " " + defaultMessage;
    }
  }
}
