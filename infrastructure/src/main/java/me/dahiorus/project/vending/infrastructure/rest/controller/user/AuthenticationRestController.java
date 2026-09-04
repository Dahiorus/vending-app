package me.dahiorus.project.vending.infrastructure.rest.controller.user;

import static me.dahiorus.project.vending.infrastructure.security.jwt.JwtTokenIssuer.REFRESH_TOKEN_TYPE;
import static me.dahiorus.project.vending.infrastructure.security.jwt.JwtTokenIssuer.TOKEN_TYPE_CLAIM;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.Role;
import me.dahiorus.project.vending.domain.user.port.UserWithRolesRepositoryPort;
import me.dahiorus.project.vending.infrastructure.rest.entity.user.AuthenticateRequestDto;
import me.dahiorus.project.vending.infrastructure.rest.entity.user.AuthenticateResponseDto;
import me.dahiorus.project.vending.infrastructure.rest.entity.user.RefreshTokenRequestDto;
import me.dahiorus.project.vending.infrastructure.security.jwt.JwtTokenIssuer;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public")
@RestController
@RequestMapping("/api/v1/authenticate")
public class AuthenticationRestController {

  private final AuthenticationManager authenticationManager;
  private final JwtTokenIssuer tokenIssuer;
  private final JwtDecoder jwtDecoder;
  private final UserWithRolesRepositoryPort userWithRolesRepository;

  public AuthenticationRestController(
      final AuthenticationManager authenticationManager,
      final JwtTokenIssuer tokenIssuer,
      final JwtDecoder jwtDecoder,
      final UserWithRolesRepositoryPort userWithRolesRepository) {
    this.authenticationManager = authenticationManager;
    this.tokenIssuer = tokenIssuer;
    this.jwtDecoder = jwtDecoder;
    this.userWithRolesRepository = userWithRolesRepository;
  }

  @Operation(description = "Authenticate a user")
  @ApiResponse(responseCode = "200", description = "User authenticated")
  @ApiResponse(responseCode = "401", description = "Bad credentials")
  @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  public ResponseEntity<AuthenticateResponseDto> authenticate(
      @RequestBody final AuthenticateRequestDto authRequest) {
    var authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                authRequest.username(), authRequest.password()));
    var user = (UserDetails) authentication.getPrincipal();

    var accessToken = tokenIssuer.createAccessToken(user.getUsername(), user.getAuthorities());
    var refreshToken = tokenIssuer.createRefreshToken(user.getUsername());

    return ok(new AuthenticateResponseDto(accessToken, refreshToken));
  }

  @Operation(description = "Refresh a user access token")
  @ApiResponse(responseCode = "200", description = "Access token refreshed")
  @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
  @PostMapping(
      value = "/refresh",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE)
  public ResponseEntity<AuthenticateResponseDto> refreshToken(
      @RequestBody final RefreshTokenRequestDto request) throws ResourceNotFound {

    var refreshJwt = decodeRefreshToken(request.token());
    var username = refreshJwt.getSubject();
    var user = userWithRolesRepository.getByUsername(EmailAddress.of(username));

    var accessToken =
        tokenIssuer.createAccessToken(
            username,
            user.roles().stream().map(Role::asRole).map(SimpleGrantedAuthority::new).toList());

    return ok(new AuthenticateResponseDto(accessToken, request.token()));
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
}
