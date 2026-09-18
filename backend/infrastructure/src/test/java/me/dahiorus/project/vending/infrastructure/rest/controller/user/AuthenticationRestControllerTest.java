package me.dahiorus.project.vending.infrastructure.rest.controller.user;

import static java.time.temporal.ChronoUnit.DAYS;
import static me.dahiorus.project.vending.infrastructure.security.jwt.JwtTokenIssuer.ACCESS_TOKEN_TYPE;
import static me.dahiorus.project.vending.infrastructure.security.jwt.JwtTokenIssuer.REFRESH_TOKEN_TYPE;
import static me.dahiorus.project.vending.infrastructure.security.jwt.JwtTokenIssuer.TOKEN_TYPE_CLAIM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import me.dahiorus.project.vending.domain.exception.InvalidRefreshToken;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.Password;
import me.dahiorus.project.vending.domain.user.entity.RefreshTokenId;
import me.dahiorus.project.vending.domain.user.entity.Role;
import me.dahiorus.project.vending.domain.user.entity.UserId;
import me.dahiorus.project.vending.domain.user.entity.UserWithRoles;
import me.dahiorus.project.vending.domain.user.port.RefreshTokenApiPort;
import me.dahiorus.project.vending.domain.user.port.UserWithRolesRepositoryPort;
import me.dahiorus.project.vending.infrastructure.rest.controller.user.AuthenticationRestControllerTest.TestConfig;
import me.dahiorus.project.vending.infrastructure.rest.entity.user.AuthenticateRequestDto;
import me.dahiorus.project.vending.infrastructure.rest.exception.RestResponseExceptionHandler;
import me.dahiorus.project.vending.infrastructure.security.config.CorsProperties;
import me.dahiorus.project.vending.infrastructure.security.config.WebSecurityConfig;
import me.dahiorus.project.vending.infrastructure.security.cookie.RefreshTokenCookieFactory;
import me.dahiorus.project.vending.infrastructure.security.cookie.RefreshTokenCookieProperties;
import me.dahiorus.project.vending.infrastructure.security.jwt.IssuedRefreshToken;
import me.dahiorus.project.vending.infrastructure.security.jwt.JwtProperties;
import me.dahiorus.project.vending.infrastructure.security.jwt.JwtTokenIssuer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthenticationRestController.class)
@Import({RestResponseExceptionHandler.class, WebSecurityConfig.class, TestConfig.class})
@TestPropertySource(properties = "app.cors.allowed-origins=https://spa.example.test")
class AuthenticationRestControllerTest {

  private static final String USERNAME = "user@test.org";
  private static final String PASSWORD = "secret-password";
  private static final String ACCESS_TOKEN = "access.jwt";
  private static final String REFRESH_TOKEN = "refresh.jwt";
  private static final String NEW_REFRESH_TOKEN = "new-refresh.jwt";
  private static final String REFRESH_TOKEN_JTI = "11111111-1111-1111-1111-111111111111";
  private static final String NEW_REFRESH_TOKEN_JTI = "22222222-2222-2222-2222-222222222222";
  private static final String COOKIE_NAME = "refresh_token";
  private static final Instant NOW = Instant.parse("2026-09-15T15:35:17Z");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private AuthenticationManager authenticationManager;
  @MockitoBean private JwtTokenIssuer tokenIssuer;
  @MockitoBean private JwtDecoder jwtDecoder;
  @MockitoBean private UserWithRolesRepositoryPort userWithRolesRepository;
  @MockitoBean private RefreshTokenApiPort refreshTokenApiPort;
  @MockitoBean private JwtAuthenticationConverter jwtAuthenticationConverter;

  @Test
  void should_authenticate_and_set_the_refresh_cookie() throws Exception {
    // Given
    var user =
        User.withUsername(USERNAME)
            .password(PASSWORD)
            .authorities("ROLE_USER", "ROLE_ADMIN")
            .build();
    var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
    when(tokenIssuer.createAccessToken(eq(USERNAME), any())).thenReturn(ACCESS_TOKEN);
    when(tokenIssuer.createRefreshToken(USERNAME))
        .thenReturn(new IssuedRefreshToken(REFRESH_TOKEN, REFRESH_TOKEN_JTI, NOW.plus(365, DAYS)));

    // When / Then
    mockMvc
        .perform(
            post("/api/v1/authenticate")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new AuthenticateRequestDto(USERNAME, PASSWORD))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value(ACCESS_TOKEN))
        .andExpect(jsonPath("$.refreshToken").doesNotExist())
        .andExpect(jsonPath("$.username").doesNotExist())
        .andExpect(jsonPath("$.password").doesNotExist())
        .andExpect(cookie().value(COOKIE_NAME, REFRESH_TOKEN))
        .andExpect(cookie().httpOnly(COOKIE_NAME, true));

    verify(refreshTokenApiPort).save(any());

    var authenticationCaptor = ArgumentCaptor.forClass(Authentication.class);
    verify(authenticationManager).authenticate(authenticationCaptor.capture());
    assertThat(authenticationCaptor.getValue())
        .isInstanceOfSatisfying(
            UsernamePasswordAuthenticationToken.class,
            token -> {
              assertThat(token.getPrincipal()).isEqualTo(USERNAME);
              assertThat(token.getCredentials()).isEqualTo(PASSWORD);
            });

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Collection<? extends GrantedAuthority>> authoritiesCaptor =
        ArgumentCaptor.forClass(Collection.class);
    verify(tokenIssuer).createAccessToken(eq(USERNAME), authoritiesCaptor.capture());
    assertThat(authoritiesCaptor.getValue())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
  }

  @Test
  void should_return_unauthorized_when_authentication_fails() throws Exception {
    // Given
    when(authenticationManager.authenticate(any(Authentication.class)))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    // When / Then
    mockMvc
        .perform(
            post("/api/v1/authenticate")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new AuthenticateRequestDto(USERNAME, PASSWORD))))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.message").value("Bad credentials"))
        .andExpect(jsonPath("$.accessToken").doesNotExist())
        .andExpect(jsonPath("$.password").doesNotExist());
  }

  @Test
  void should_refresh_access_token_and_rotate_the_refresh_cookie() throws Exception {
    // Given
    var jwt = refreshJwt(REFRESH_TOKEN, USERNAME, REFRESH_TOKEN_JTI);
    var user =
        new UserWithRoles(
            new UserId(UUID.randomUUID()),
            EmailAddress.of(USERNAME),
            Password.of("hashed-password"),
            Set.of(new Role("admin")));
    when(jwtDecoder.decode(REFRESH_TOKEN)).thenReturn(jwt);
    when(userWithRolesRepository.getByUsername(EmailAddress.of(USERNAME))).thenReturn(user);
    when(tokenIssuer.createAccessToken(eq(USERNAME), any())).thenReturn(ACCESS_TOKEN);
    when(tokenIssuer.createRefreshToken(USERNAME))
        .thenReturn(
            new IssuedRefreshToken(NEW_REFRESH_TOKEN, NEW_REFRESH_TOKEN_JTI, NOW.plus(365, DAYS)));
    when(refreshTokenApiPort.rotate(any(), any()))
        .thenAnswer(invocation -> invocation.getArgument(1));

    // When / Then
    mockMvc
        .perform(
            post("/api/v1/authenticate/refresh")
                .with(csrf())
                .cookie(new Cookie(COOKIE_NAME, REFRESH_TOKEN)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value(ACCESS_TOKEN))
        .andExpect(cookie().value(COOKIE_NAME, NEW_REFRESH_TOKEN));

    verify(refreshTokenApiPort)
        .rotate(eq(new RefreshTokenId(UUID.fromString(REFRESH_TOKEN_JTI))), any());

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Collection<? extends GrantedAuthority>> authoritiesCaptor =
        ArgumentCaptor.forClass(Collection.class);
    verify(tokenIssuer).createAccessToken(eq(USERNAME), authoritiesCaptor.capture());
    assertThat(authoritiesCaptor.getValue())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactly("ROLE_ADMIN");
  }

  @Test
  void should_reject_refresh_without_a_cookie() throws Exception {
    mockMvc
        .perform(post("/api/v1/authenticate/refresh").with(csrf()))
        .andExpect(status().isUnauthorized());

    verifyNoInteractions(jwtDecoder, userWithRolesRepository, refreshTokenApiPort);
  }

  @Test
  void should_reject_refresh_when_token_is_not_a_refresh_token() throws Exception {
    // Given
    when(jwtDecoder.decode(ACCESS_TOKEN)).thenReturn(accessJwt(ACCESS_TOKEN, USERNAME));

    // When / Then
    mockMvc
        .perform(
            post("/api/v1/authenticate/refresh")
                .with(csrf())
                .cookie(new Cookie(COOKIE_NAME, ACCESS_TOKEN)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.message").value("The provided token is not a refresh token"))
        .andExpect(jsonPath("$.accessToken").doesNotExist());

    verifyNoInteractions(userWithRolesRepository, refreshTokenApiPort);
  }

  @Test
  void should_reject_refresh_when_token_is_invalid_or_expired() throws Exception {
    // Given
    when(jwtDecoder.decode(REFRESH_TOKEN)).thenThrow(new JwtException("expired"));

    // When / Then
    mockMvc
        .perform(
            post("/api/v1/authenticate/refresh")
                .with(csrf())
                .cookie(new Cookie(COOKIE_NAME, REFRESH_TOKEN)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.message").value("Invalid or expired refresh token"))
        .andExpect(jsonPath("$.accessToken").doesNotExist());

    verifyNoInteractions(userWithRolesRepository, refreshTokenApiPort);
  }

  @Test
  void should_return_not_found_when_refresh_user_no_longer_exists() throws Exception {
    // Given
    when(jwtDecoder.decode(REFRESH_TOKEN))
        .thenReturn(refreshJwt(REFRESH_TOKEN, USERNAME, REFRESH_TOKEN_JTI));
    when(userWithRolesRepository.getByUsername(EmailAddress.of(USERNAME)))
        .thenThrow(new ResourceNotFound("User not found"));

    // When / Then
    mockMvc
        .perform(
            post("/api/v1/authenticate/refresh")
                .with(csrf())
                .cookie(new Cookie(COOKIE_NAME, REFRESH_TOKEN)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.message").value("User not found"))
        .andExpect(jsonPath("$.accessToken").doesNotExist());
  }

  @Test
  void should_reject_refresh_and_clear_the_cookie_when_the_token_is_reused() throws Exception {
    // Given
    when(jwtDecoder.decode(REFRESH_TOKEN))
        .thenReturn(refreshJwt(REFRESH_TOKEN, USERNAME, REFRESH_TOKEN_JTI));
    var user =
        new UserWithRoles(
            new UserId(UUID.randomUUID()),
            EmailAddress.of(USERNAME),
            Password.of("hashed-password"),
            Set.of(new Role("admin")));
    when(userWithRolesRepository.getByUsername(EmailAddress.of(USERNAME))).thenReturn(user);
    when(tokenIssuer.createAccessToken(eq(USERNAME), any())).thenReturn(ACCESS_TOKEN);
    when(tokenIssuer.createRefreshToken(USERNAME))
        .thenReturn(
            new IssuedRefreshToken(NEW_REFRESH_TOKEN, NEW_REFRESH_TOKEN_JTI, NOW.plus(365, DAYS)));
    when(refreshTokenApiPort.rotate(any(), any()))
        .thenThrow(new InvalidRefreshToken("Refresh token already used"));

    // When / Then
    mockMvc
        .perform(
            post("/api/v1/authenticate/refresh")
                .with(csrf())
                .cookie(new Cookie(COOKIE_NAME, REFRESH_TOKEN)))
        .andExpect(status().isUnauthorized())
        .andExpect(cookie().maxAge(COOKIE_NAME, 0));
  }

  private static Jwt refreshJwt(String tokenValue, String subject, String jti) {
    return jwt(tokenValue, subject, REFRESH_TOKEN_TYPE, jti);
  }

  private static Jwt accessJwt(String tokenValue, String subject) {
    return jwt(tokenValue, subject, ACCESS_TOKEN_TYPE, UUID.randomUUID().toString());
  }

  private static Jwt jwt(String tokenValue, String subject, String tokenType, String jti) {
    return Jwt.withTokenValue(tokenValue)
        .header("alg", "none")
        .subject(subject)
        .issuedAt(NOW)
        .expiresAt(NOW.plusSeconds(3600))
        .claim(org.springframework.security.oauth2.jwt.JwtClaimNames.JTI, jti)
        .claim(TOKEN_TYPE_CLAIM, tokenType)
        .build();
  }

  static class TestConfig {
    @Bean
    Clock clock() {
      return Clock.fixed(NOW, ZoneOffset.UTC);
    }

    @Bean
    RefreshTokenCookieProperties refreshTokenCookieProperties() {
      return new RefreshTokenCookieProperties();
    }

    @Bean
    RefreshTokenCookieFactory refreshTokenCookieFactory(
        final RefreshTokenCookieProperties properties) {
      return new RefreshTokenCookieFactory(properties);
    }

    @Bean
    @Primary
    CorsProperties testCorsProperties() {
      CorsProperties properties = new CorsProperties();
      properties.setAllowedOrigins(List.of("https://spa.example.test"));
      return properties;
    }

    @Bean
    JwtProperties jwtProperties() {
      JwtProperties properties = new JwtProperties();
      properties.setIssuerUri("https://issuer.example.test");
      return properties;
    }
  }
}
