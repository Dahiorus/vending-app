package me.dahiorus.project.vending.infrastructure.security.cookie;

import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieFactory {
  private final RefreshTokenCookieProperties properties;

  public RefreshTokenCookieFactory(final RefreshTokenCookieProperties properties) {
    this.properties = properties;
  }

  public ResponseCookie create(final String value, final Duration maxAge) {
    return cookieBuilder(value).maxAge(maxAge).build();
  }

  public ResponseCookie clear() {
    return cookieBuilder("").maxAge(Duration.ZERO).build();
  }

  private ResponseCookie.ResponseCookieBuilder cookieBuilder(final String value) {
    var builder =
        ResponseCookie.from(properties.getName(), value)
            .httpOnly(true)
            .secure(properties.isSecure())
            .path(properties.getPath())
            .sameSite(properties.getSameSite());

    if (properties.getDomain() != null && !properties.getDomain().isBlank()) {
      builder.domain(properties.getDomain());
    }

    return builder;
  }
}
