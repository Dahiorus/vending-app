package me.dahiorus.project.vending.infrastructure.rest.controller.user;

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
import me.dahiorus.project.vending.infrastructure.rest.exception.InvalidTokenCreation;
import me.dahiorus.project.vending.infrastructure.rest.exception.UnparsableToken;
import me.dahiorus.project.vending.infrastructure.security.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public")
@RestController
@RequestMapping("/api/v1/authenticate")
public class AuthenticationRestController {

  private final TokenService tokenService;
  private final UserWithRolesRepositoryPort userWithRolesRepository;

  public AuthenticationRestController(
      final TokenService tokenService, final UserWithRolesRepositoryPort userWithRolesRepository) {
    this.tokenService = tokenService;
    this.userWithRolesRepository = userWithRolesRepository;
  }

  @Operation(description = "Authenticate a user")
  @ApiResponse(responseCode = "200", description = "User authenticated")
  @ApiResponse(responseCode = "401", description = "Bad credentials")
  @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  public ResponseEntity<AuthenticateResponseDto> authenticate(
      @RequestBody final AuthenticateRequestDto authRequest) {
    // marker method
    // the authentication is done in JwtAuthenticationFilter
    return ok(null);
  }

  @Operation(description = "Refresh a user access token")
  @ApiResponse(responseCode = "200", description = "Access token refreshed")
  @PostMapping(
      value = "/refresh",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE)
  public ResponseEntity<AuthenticateResponseDto> refreshToken(
      @RequestBody final RefreshTokenRequestDto request)
      throws UnparsableToken, InvalidTokenCreation, ResourceNotFound {

    var authentication = tokenService.parseToken(request.token());
    var username = (String) authentication.getPrincipal();
    var user = userWithRolesRepository.getByUsername(EmailAddress.of(username));

    var accessToken =
        tokenService.createAccessToken(
            username,
            user.roles().stream().map(Role::asRole).map(SimpleGrantedAuthority::new).toList());

    return ok(new AuthenticateResponseDto(accessToken, request.token()));
  }
}
