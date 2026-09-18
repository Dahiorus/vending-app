package me.dahiorus.project.vending.infrastructure.rest.controller.user;

import static me.dahiorus.project.vending.infrastructure.security.jwt.JwtTokenIssuer.REFRESH_TOKEN_TYPE;
import static me.dahiorus.project.vending.infrastructure.security.jwt.JwtTokenIssuer.TOKEN_TYPE_CLAIM;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import me.dahiorus.project.vending.domain.exception.InvalidRefreshToken;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.RefreshToken;
import me.dahiorus.project.vending.domain.user.entity.RefreshTokenId;
import me.dahiorus.project.vending.domain.user.entity.Role;
import me.dahiorus.project.vending.domain.user.port.RefreshTokenApiPort;
import me.dahiorus.project.vending.domain.user.port.RefreshTokenRepositoryPort;
import me.dahiorus.project.vending.domain.user.port.UserWithRolesRepositoryPort;
import me.dahiorus.project.vending.infrastructure.rest.entity.user.AuthenticateRequestDto;
import me.dahiorus.project.vending.infrastructure.rest.entity.user.AuthenticateResponseDto;
import me.dahiorus.project.vending.infrastructure.security.cookie.RefreshTokenCookieFactory;
import me.dahiorus.project.vending.infrastructure.security.jwt.JwtTokenIssuer;
import me.dahiorus.project.vending.infrastructure.security.jwt.JwtTokenIssuer.IssuedRefreshToken;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public")
@RestController
@RequestMapping("/api/v1/authenticate")
public class AuthenticationRestController {

  private static final String REFRESH_COOKIE_NAME = "${app.refresh-token-cookie.name}";

  private final AuthenticationManager authenticationManager;
  private final JwtTokenIssuer tokenIssuer;
  private final JwtDecoder jwtDecoder;
  private final UserWithRolesRepositoryPort userWithRolesRepository;
  private final RefreshTokenApiPort refreshTokenApiPort;
  private final RefreshTokenRepositoryPort refreshTokenRepository;
  private final RefreshTokenCookieFactory refreshTokenCookieFactory;

  public AuthenticationRestController(
      final AuthenticationManager authenticationManager,
      final JwtTokenIssuer tokenIssuer,
      final JwtDecoder jwtDecoder,
      final UserWithRolesRepositoryPort userWithRolesRepository,
      final RefreshTokenApiPort refreshTokenApiPort,
      final RefreshTokenRepositoryPort refreshTokenRepository,
      final RefreshTokenCookieFactory refreshTokenCookieFactory) {
    this.authenticationManager = authenticationManager;
    this.tokenIssuer = tokenIssuer;
    this.jwtDecoder = jwtDecoder;
    this.userWithRolesRepository = userWithRolesRepository;
    this.refreshTokenApiPort = refreshTokenApiPort;
    this.refreshTokenRepository = refreshTokenRepository;
    this.refreshTokenCookieFactory = refreshTokenCookieFactory;
  }

  @Operation(description = "Authenticate a user")
  @ApiResponse(responseCode = "200", description = "User authenticated")
  @ApiResponse(responseCode = "401", description = "Bad credentials")
  @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  public ResponseEntity<AuthenticateResponseDto> authenticate(
      @RequestBody final AuthenticateRequestDto authRequest, final HttpServletRequest request) {
    // login is excluded from required CSRF protection, so the deferred CsrfToken must be
    // materialized explicitly to force the CsrfFilter to deposit the XSRF-TOKEN cookie
    if (request.getAttribute(CsrfToken.class.getName()) instanceof CsrfToken csrfToken) {
      csrfToken.getToken();
    }

    var authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                authRequest.username(), authRequest.password()));
    var user = (UserDetails) authentication.getPrincipal();

    var accessToken = tokenIssuer.createAccessToken(user.getUsername(), user.getAuthorities());
    var issuedRefreshToken = tokenIssuer.createRefreshToken(user.getUsername());

    refreshTokenRepository.save(newRefreshTokenEntity(user.getUsername(), issuedRefreshToken));

    return ok().header(SET_COOKIE, refreshCookie(issuedRefreshToken).toString())
        .body(new AuthenticateResponseDto(accessToken));
  }

  @Operation(description = "Refresh a user access token")
  @ApiResponse(responseCode = "200", description = "Access token refreshed")
  @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
  @PostMapping(value = "/refresh", produces = APPLICATION_JSON_VALUE)
  public ResponseEntity<AuthenticateResponseDto> refreshToken(
      @Parameter(in = ParameterIn.COOKIE, name = "refresh_token")
          @CookieValue(name = REFRESH_COOKIE_NAME, required = false)
          final String refreshCookie)
      throws ResourceNotFound {
    if (refreshCookie == null) {
      throw new BadCredentialsException("Missing refresh token cookie");
    }

    var refreshJwt = decodeRefreshToken(refreshCookie);
    var username = refreshJwt.getSubject();
    var user = userWithRolesRepository.getByUsername(EmailAddress.of(username));

    var accessToken =
        tokenIssuer.createAccessToken(
            username,
            user.roles().stream().map(Role::asRole).map(SimpleGrantedAuthority::new).toList());
    var issuedRefreshToken = tokenIssuer.createRefreshToken(username);

    try {
      refreshTokenApiPort.rotate(
          new RefreshTokenId(UUID.fromString(refreshJwt.getId())),
          newRefreshTokenEntity(username, issuedRefreshToken));
    } catch (InvalidRefreshToken e) {
      return status(UNAUTHORIZED)
          .header(SET_COOKIE, refreshTokenCookieFactory.clear().toString())
          .build();
    }

    return ok().header(SET_COOKIE, refreshCookie(issuedRefreshToken).toString())
        .body(new AuthenticateResponseDto(accessToken));
  }

  @Operation(description = "Log a user out and revoke their refresh token")
  @ApiResponse(responseCode = "204", description = "User logged out")
  @PostMapping("/logout")
  public void logout() {
    // marker method for Swagger
  }

  private Jwt decodeRefreshToken(final String token) {
    Jwt jwt;
    try {
      jwt = jwtDecoder.decode(token);
    } catch (JwtException e) {
      throw new BadCredentialsException("Invalid or expired refresh token", e);
    }

    if (!REFRESH_TOKEN_TYPE.equals(jwt.getClaimAsString(TOKEN_TYPE_CLAIM))) {
      throw new BadCredentialsException("The provided token is not a refresh token");
    }

    return jwt;
  }

  private static RefreshToken newRefreshTokenEntity(
      final String username, final IssuedRefreshToken issuedRefreshToken) {
    return new RefreshToken(
        new RefreshTokenId(UUID.fromString(issuedRefreshToken.jti())),
        EmailAddress.of(username),
        Instant.now(),
        issuedRefreshToken.expiresAt(),
        false);
  }

  private ResponseCookie refreshCookie(final IssuedRefreshToken issuedRefreshToken) {
    return refreshTokenCookieFactory.create(
        issuedRefreshToken.value(),
        Duration.between(Instant.now(), issuedRefreshToken.expiresAt()));
  }
}
