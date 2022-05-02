package me.dahiorus.project.vending.web.config;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.ChronoUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "jwt")
public class JwtProperties
{
  @Getter
  @Setter
  private String secret;

  @Getter
  @Setter
  private String issuerUri;

  @Getter
  @Setter
  private Duration accessTokenDuration = Duration.ofDays(1);

  @Getter
  @Setter
  @DurationUnit(ChronoUnit.DAYS)
  private Period refreshTokenDuration = Period.ofDays(365);
}
