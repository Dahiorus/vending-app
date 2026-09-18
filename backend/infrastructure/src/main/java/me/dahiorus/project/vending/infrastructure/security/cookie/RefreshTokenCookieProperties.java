package me.dahiorus.project.vending.infrastructure.security.cookie;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.refresh-token-cookie")
@Validated
public class RefreshTokenCookieProperties {
  /** Cookie name used to store the refresh token. */
  private String name = "refresh_token";

  /** Cookie path limiting where the refresh token cookie is sent. */
  private String path = "/api/v1/authenticate";

  /** Whether the refresh token cookie is only sent over HTTPS. */
  private boolean secure = true;

  /** SameSite policy applied to the refresh token cookie. */
  private String sameSite = "Strict";

  /** Optional cookie domain. Leave blank to keep the browser default host-only scope. */
  private String domain;

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath(final String path) {
    this.path = path;
  }

  public boolean isSecure() {
    return secure;
  }

  public void setSecure(final boolean secure) {
    this.secure = secure;
  }

  public String getSameSite() {
    return sameSite;
  }

  public void setSameSite(final String sameSite) {
    this.sameSite = sameSite;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(final String domain) {
    this.domain = domain;
  }
}
