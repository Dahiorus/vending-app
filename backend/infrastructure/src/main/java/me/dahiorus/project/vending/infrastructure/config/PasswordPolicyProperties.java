package me.dahiorus.project.vending.infrastructure.config;

import me.dahiorus.project.vending.domain.user.port.PasswordPolicy;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.password-policy")
public record PasswordPolicyProperties(
    Integer minLength,
    Integer maxLength,
    Integer minLowerCaseCharCount,
    Integer minUpperCaseCharCount,
    Integer minDigitCount,
    Integer minSpecialCharCount)
    implements PasswordPolicy {}
