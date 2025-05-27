package me.dahiorus.project.vending.infrastructure.security.jwt;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
  private String secret;

  private String issuerUri;

  private Duration accessTokenDuration = Duration.ofHours(1);

  @DurationUnit(ChronoUnit.DAYS)
  private Period refreshTokenDuration = Period.ofDays(365);

  public String getSecret() {
    return secret;
  }

  public void setSecret(final String secret) {
    this.secret = secret;
  }

  public String getIssuerUri() {
    return issuerUri;
  }

  public void setIssuerUri(final String issuerUri) {
    this.issuerUri = issuerUri;
  }

  public Duration getAccessTokenDuration() {
    return accessTokenDuration;
  }

  public void setAccessTokenDuration(final Duration accessTokenDuration) {
    this.accessTokenDuration = accessTokenDuration;
  }

  public Period getRefreshTokenDuration() {
    return refreshTokenDuration;
  }

  public void setRefreshTokenDuration(final Period refreshTokenDuration) {
    this.refreshTokenDuration = refreshTokenDuration;
  }
}
