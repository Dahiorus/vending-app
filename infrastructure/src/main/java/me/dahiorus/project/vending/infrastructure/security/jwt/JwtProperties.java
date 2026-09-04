package me.dahiorus.project.vending.infrastructure.security.jwt;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "jwt")
@Validated
public class JwtProperties {
  @NotBlank private String issuerUri;

  private Duration accessTokenDuration = Duration.ofHours(1);

  @DurationUnit(ChronoUnit.DAYS)
  private Period refreshTokenDuration = Period.ofDays(365);

  /** PEM-encoded RSA public key. When blank, an ephemeral key pair is generated at startup. */
  private String publicKey;

  /** PEM-encoded RSA private key. When blank, an ephemeral key pair is generated at startup. */
  private String privateKey;

  public String getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(final String publicKey) {
    this.publicKey = publicKey;
  }

  public String getPrivateKey() {
    return privateKey;
  }

  public void setPrivateKey(final String privateKey) {
    this.privateKey = privateKey;
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
