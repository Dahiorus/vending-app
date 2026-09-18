package me.dahiorus.project.vending.infrastructure.security.cookie;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class RefreshTokenCookieFactoryTest {
  @Test
  void should_create_refresh_token_cookie_with_configured_attributes() {
    var properties = new RefreshTokenCookieProperties();
    properties.setName("refresh_cookie");
    properties.setPath("/api/v1/authenticate/refresh");
    properties.setSecure(true);
    properties.setSameSite("Strict");

    var factory = new RefreshTokenCookieFactory(properties);

    var cookie = factory.create("refresh-token-value", Duration.ofMinutes(15));

    assertThat(cookie.getName()).isEqualTo("refresh_cookie");
    assertThat(cookie.getValue()).isEqualTo("refresh-token-value");
    assertThat(cookie.isHttpOnly()).isTrue();
    assertThat(cookie.isSecure()).isTrue();
    assertThat(cookie.getPath()).isEqualTo("/api/v1/authenticate/refresh");
    assertThat(cookie.getSameSite()).isEqualTo("Strict");
    assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofMinutes(15));
  }

  @Test
  void should_clear_refresh_token_cookie() {
    var properties = new RefreshTokenCookieProperties();
    properties.setName("refresh_cookie");
    properties.setPath("/api/v1/authenticate/refresh");
    properties.setSecure(false);
    properties.setSameSite("Lax");

    var factory = new RefreshTokenCookieFactory(properties);

    var cookie = factory.clear();

    assertThat(cookie.getName()).isEqualTo("refresh_cookie");
    assertThat(cookie.getValue()).isEmpty();
    assertThat(cookie.isHttpOnly()).isTrue();
    assertThat(cookie.isSecure()).isFalse();
    assertThat(cookie.getPath()).isEqualTo("/api/v1/authenticate/refresh");
    assertThat(cookie.getSameSite()).isEqualTo("Lax");
    assertThat(cookie.getMaxAge()).isLessThanOrEqualTo(Duration.ZERO);
  }
}
