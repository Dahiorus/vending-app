package me.dahiorus.project.vending.infrastructure.security.cookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import java.util.UUID;
import me.dahiorus.project.vending.domain.user.entity.RefreshTokenId;
import me.dahiorus.project.vending.domain.user.port.RefreshTokenApiPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

class RefreshTokenLogoutHandlerTest {

  private static final String COOKIE_NAME = "refresh_token";
  private static final String REFRESH_TOKEN = "refresh.jwt";
  private static final String REFRESH_TOKEN_JTI = "11111111-1111-1111-1111-111111111111";

  private JwtDecoder jwtDecoder;
  private RefreshTokenApiPort refreshTokenApiPort;
  private RefreshTokenLogoutHandler handler;

  @BeforeEach
  void setUp() {
    jwtDecoder = mock(JwtDecoder.class);
    refreshTokenApiPort = mock(RefreshTokenApiPort.class);

    RefreshTokenCookieProperties properties = new RefreshTokenCookieProperties();
    properties.setName(COOKIE_NAME);
    handler =
        new RefreshTokenLogoutHandler(
            jwtDecoder, refreshTokenApiPort, new RefreshTokenCookieFactory(properties), properties);
  }

  @Test
  void should_revoke_the_token_and_clear_the_cookie() {
    // Given
    when(jwtDecoder.decode(REFRESH_TOKEN))
        .thenReturn(
            Jwt.withTokenValue(REFRESH_TOKEN)
                .header("alg", "none")
                .subject("user@test.org")
                .claim("jti", REFRESH_TOKEN_JTI)
                .issuedAt(java.time.Instant.now())
                .expiresAt(java.time.Instant.now().plusSeconds(3600))
                .build());
    var request = new MockHttpServletRequest();
    request.setCookies(new Cookie(COOKIE_NAME, REFRESH_TOKEN));
    var response = new MockHttpServletResponse();

    // When
    handler.logout(request, response, null);

    // Then
    verify(refreshTokenApiPort).revoke(new RefreshTokenId(UUID.fromString(REFRESH_TOKEN_JTI)));
    assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).contains(COOKIE_NAME + "=;", "Max-Age=0");
  }

  @Test
  void should_clear_the_cookie_without_calling_the_port_when_no_cookie_is_present() {
    var request = new MockHttpServletRequest();
    var response = new MockHttpServletResponse();

    handler.logout(request, response, null);

    verify(refreshTokenApiPort, never()).revoke(any());
    assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).contains(COOKIE_NAME + "=;", "Max-Age=0");
  }

  @Test
  void should_clear_the_cookie_and_never_fail_when_the_cookie_is_invalid() {
    when(jwtDecoder.decode(REFRESH_TOKEN)).thenThrow(new JwtException("invalid"));
    var request = new MockHttpServletRequest();
    request.setCookies(new Cookie(COOKIE_NAME, REFRESH_TOKEN));
    var response = new MockHttpServletResponse();

    handler.logout(request, response, null);

    verify(refreshTokenApiPort, never()).revoke(any());
    assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).contains(COOKIE_NAME + "=;", "Max-Age=0");
  }
}
