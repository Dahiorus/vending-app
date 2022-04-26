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
}
