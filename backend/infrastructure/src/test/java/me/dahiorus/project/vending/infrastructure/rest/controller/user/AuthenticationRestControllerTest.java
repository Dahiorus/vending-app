package me.dahiorus.project.vending.infrastructure.rest.controller.user;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.Role;
import me.dahiorus.project.vending.domain.user.entity.UserId;
import me.dahiorus.project.vending.domain.user.entity.UserWithRoles;
import me.dahiorus.project.vending.domain.user.port.UserWithRolesRepositoryPort;
import me.dahiorus.project.vending.infrastructure.rest.entity.user.AuthenticateRequestDto;
import me.dahiorus.project.vending.infrastructure.rest.entity.user.RefreshTokenRequestDto;
import me.dahiorus.project.vending.infrastructure.rest.exception.RestResponseExceptionHandler;
import me.dahiorus.project.vending.infrastructure.security.config.CorsProperties;
import me.dahiorus.project.vending.infrastructure.security.config.WebSecurityConfig;
import me.dahiorus.project.vending.infrastructure.security.jwt.JwtTokenIssuer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthenticationRestController.class)
@Import({
  RestResponseExceptionHandler.class,
  WebSecurityConfig.class,
  AuthenticationRestControllerTest.FixedClockConfig.class
})
class AuthenticationRestControllerTest {

  private static final String USERNAME = "user@test.org";
  private static final String PASSWORD = "secret-password";
  private static final String ACCESS_TOKEN = "access.jwt";
  private static final String REFRESH_TOKEN = "refresh.jwt";
  private static final Instant NOW = Instant.parse("2026-09-15T15:35:17Z");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private AuthenticationManager authenticationManager;
  @MockitoBean private JwtTokenIssuer tokenIssuer;
  @MockitoBean private JwtDecoder jwtDecoder;
  @MockitoBean private UserWithRolesRepositoryPort userWithRolesRepository;
  @MockitoBean private JwtAuthenticationConverter jwtAuthenticationConverter;
  @MockitoBean private CorsProperties corsProperties;

  @Test
  void should_authenticate_and_return_issued_tokens() throws Exception {
    // Given
    var user =
        User.withUsername(USERNAME)
            .password(PASSWORD)
            .authorities("ROLE_USER", "ROLE_ADMIN")
            .build();
    var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
    when(tokenIssuer.createAccessToken(eq(USERNAME), any())).thenReturn(ACCESS_TOKEN);
    when(tokenIssuer.createRefreshToken(USERNAME)).thenReturn(REFRESH_TOKEN);

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
        .andExpect(jsonPath("$.refreshToken").value(REFRESH_TOKEN))
        .andExpect(jsonPath("$.username").doesNotExist())
        .andExpect(jsonPath("$.password").doesNotExist())
        .andExpect(jsonPath("$.token").doesNotExist());

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
        .andExpect(jsonPath("$.refreshToken").doesNotExist())
        .andExpect(jsonPath("$.password").doesNotExist());
  }

  @Test
  void should_refresh_access_token_and_keep_refresh_token_unchanged() throws Exception {
    // Given
    var jwt = refreshJwt(REFRESH_TOKEN, USERNAME);
    var user =
        new UserWithRoles(
            new UserId(UUID.randomUUID()), EmailAddress.of(USERNAME), Set.of(new Role("admin")));
    when(jwtDecoder.decode(REFRESH_TOKEN)).thenReturn(jwt);
    when(userWithRolesRepository.getByUsername(EmailAddress.of(USERNAME))).thenReturn(user);
    when(tokenIssuer.createAccessToken(eq(USERNAME), any())).thenReturn(ACCESS_TOKEN);

    // When / Then
    mockMvc
        .perform(
            post("/api/v1/authenticate/refresh")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(new RefreshTokenRequestDto(REFRESH_TOKEN))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value(ACCESS_TOKEN))
        .andExpect(jsonPath("$.refreshToken").value(REFRESH_TOKEN))
        .andExpect(jsonPath("$.token").doesNotExist())
        .andExpect(jsonPath("$.password").doesNotExist());

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Collection<? extends GrantedAuthority>> authoritiesCaptor =
        ArgumentCaptor.forClass(Collection.class);
    verify(tokenIssuer).createAccessToken(eq(USERNAME), authoritiesCaptor.capture());
    assertThat(authoritiesCaptor.getValue())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactly("ROLE_ADMIN");
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
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshTokenRequestDto(ACCESS_TOKEN))))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.message").value("The provided token is not a refresh token"))
        .andExpect(jsonPath("$.accessToken").doesNotExist())
        .andExpect(jsonPath("$.refreshToken").doesNotExist());

    verifyNoInteractions(userWithRolesRepository, tokenIssuer);
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
                .contentType(APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(new RefreshTokenRequestDto(REFRESH_TOKEN))))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.message").value("Invalid or expired refresh token"))
        .andExpect(jsonPath("$.accessToken").doesNotExist())
        .andExpect(jsonPath("$.refreshToken").doesNotExist());

    verifyNoInteractions(userWithRolesRepository, tokenIssuer);
  }

  @Test
  void should_return_not_found_when_refresh_user_no_longer_exists() throws Exception {
    // Given
    when(jwtDecoder.decode(REFRESH_TOKEN)).thenReturn(refreshJwt(REFRESH_TOKEN, USERNAME));
    when(userWithRolesRepository.getByUsername(EmailAddress.of(USERNAME)))
        .thenThrow(new ResourceNotFound("User not found"));

    // When / Then
    mockMvc
        .perform(
            post("/api/v1/authenticate/refresh")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(new RefreshTokenRequestDto(REFRESH_TOKEN))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.message").value("User not found"))
        .andExpect(jsonPath("$.accessToken").doesNotExist())
        .andExpect(jsonPath("$.refreshToken").doesNotExist());
  }

  private static Jwt refreshJwt(String tokenValue, String subject) {
    return jwt(tokenValue, subject, REFRESH_TOKEN_TYPE);
  }

  private static Jwt accessJwt(String tokenValue, String subject) {
    return jwt(tokenValue, subject, ACCESS_TOKEN_TYPE);
  }

  private static Jwt jwt(String tokenValue, String subject, String tokenType) {
    return Jwt.withTokenValue(tokenValue)
        .header("alg", "none")
        .subject(subject)
        .issuedAt(NOW)
        .expiresAt(NOW.plusSeconds(3600))
        .claim(TOKEN_TYPE_CLAIM, tokenType)
        .build();
  }

  static class FixedClockConfig {
    @Bean
    Clock clock() {
      return Clock.fixed(NOW, ZoneOffset.UTC);
    }
  }
}
