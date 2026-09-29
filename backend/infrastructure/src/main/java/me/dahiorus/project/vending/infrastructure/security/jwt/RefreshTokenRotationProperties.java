package me.dahiorus.project.vending.infrastructure.security.jwt;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.refresh-token-rotation")
@Validated
public class RefreshTokenRotationProperties {

  /**
   * Window after a refresh token was rotated during which presenting the now-revoked token still
   * returns its successor instead of failing, to tolerate near-simultaneous refresh requests (e.g.
   * several browser tabs reloading at once) that race for the same single-use refresh token.
   */
  private Duration reuseGracePeriod = Duration.ofSeconds(10);

  public Duration getReuseGracePeriod() {
    return reuseGracePeriod;
  }

  public void setReuseGracePeriod(final Duration reuseGracePeriod) {
    this.reuseGracePeriod = reuseGracePeriod;
  }
}
