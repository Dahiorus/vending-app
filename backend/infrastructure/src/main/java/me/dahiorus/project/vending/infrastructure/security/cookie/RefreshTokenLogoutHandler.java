package me.dahiorus.project.vending.infrastructure.security.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import me.dahiorus.project.vending.domain.user.entity.RefreshTokenId;
import me.dahiorus.project.vending.domain.user.port.RefreshTokenApiPort;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.authentication.logout.LogoutHandler;

/**
 * Revokes the refresh token carried by the {@code refresh_token} cookie (best effort, never
 * failing on a stale/invalid cookie so that logout stays idempotent) and always clears the
 * cookie, mirroring the attributes it was originally set with.
 */
public class RefreshTokenLogoutHandler implements LogoutHandler {

  private final JwtDecoder jwtDecoder;
  private final RefreshTokenApiPort refreshTokenApiPort;
  private final RefreshTokenCookieFactory refreshTokenCookieFactory;
  private final RefreshTokenCookieProperties refreshTokenCookieProperties;

  public RefreshTokenLogoutHandler(
      final JwtDecoder jwtDecoder,
      final RefreshTokenApiPort refreshTokenApiPort,
      final RefreshTokenCookieFactory refreshTokenCookieFactory,
      final RefreshTokenCookieProperties refreshTokenCookieProperties) {
    this.jwtDecoder = jwtDecoder;
    this.refreshTokenApiPort = refreshTokenApiPort;
    this.refreshTokenCookieFactory = refreshTokenCookieFactory;
    this.refreshTokenCookieProperties = refreshTokenCookieProperties;
  }

  @Override
  public void logout(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final Authentication authentication) {
    findRefreshCookie(request).ifPresent(this::revokeBestEffort);

    response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookieFactory.clear().toString());
  }

  private Optional<String> findRefreshCookie(final HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return Optional.empty();
    }

    return Arrays.stream(cookies)
        .filter(cookie -> refreshTokenCookieProperties.getName().equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst();
  }

  private void revokeBestEffort(final String refreshCookie) {
    try {
      var jti = jwtDecoder.decode(refreshCookie).getId();
      refreshTokenApiPort.revoke(new RefreshTokenId(UUID.fromString(jti)));
    } catch (JwtException | IllegalArgumentException e) {
      // logout must be idempotent and never fail because the cookie is stale/invalid
    }
  }
}
