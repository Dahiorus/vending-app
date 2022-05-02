package me.dahiorus.project.vending.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "app.password-policy")
public class PasswordPolicyProperties
{
  /**
   * Minimum length of the password
   */
  @Getter
  @Setter
  private Integer minLength = 8;

  /**
   * Maximum length of the password
   */
  @Getter
  @Setter
  private Integer maxLength = 24;

  /**
   * Minimum lower case characters
   */
  @Getter
  @Setter
  private Integer minLowerCaseCount = 1;

  /**
   * Minimum upper case characters
   */
  @Getter
  @Setter
  private Integer minUpperCaseCount = 1;

  /**
   * Minimum digits
   */
  @Getter
  @Setter
  private Integer minDigitCount = 1;

  /**
   * Minimum special characters
   */
  @Getter
  @Setter
  private Integer minSpecialCharsCount = 0;
}
