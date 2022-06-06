package me.dahiorus.project.vending.web.api.impl;

import static me.dahiorus.project.vending.web.api.response.ResponseUtils.buildLocation;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

import java.net.URI;
import java.util.Collections;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.dto.UserDTO;
import me.dahiorus.project.vending.domain.service.UserDtoService;
import me.dahiorus.project.vending.web.api.AppWebService;
import me.dahiorus.project.vending.web.api.CreateRestAPI;
import me.dahiorus.project.vending.web.api.model.AuthenticateRequest;
import me.dahiorus.project.vending.web.api.model.AuthenticateResponse;
import me.dahiorus.project.vending.web.api.model.RefreshTokenRequest;
import me.dahiorus.project.vending.web.exception.InvalidTokenCreation;
import me.dahiorus.project.vending.web.exception.UnparsableToken;
import me.dahiorus.project.vending.web.security.JwtService;
import me.dahiorus.project.vending.web.security.SecurityConstants;

@Tag(name = "Public", description = "Public operations")
@RestController
@Log4j2
@RequiredArgsConstructor
public class PublicRestController implements CreateRestAPI<UserDTO>, AppWebService
{
  private final UserDtoService userDtoService;

  private final RepresentationModelAssembler<UserDTO, EntityModel<UserDTO>> userModelAssembler;

  private final JwtService jwtService;

  @Override
  @Operation(description = "Register a user")
  @ApiResponse(responseCode = "201", description = "User registered")
  @PostMapping(value = SecurityConstants.REGISTER_ENDPOINT, consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaTypes.HAL_JSON_VALUE)
  public ResponseEntity<EntityModel<UserDTO>> create(@RequestBody final UserDTO user)
      throws ValidationException
  {
    log.debug("Signing up a new user");

    user.setRoles(Collections.emptyList()); // setting roles is forbidden in registration
    UserDTO createdUser = userDtoService.create(user);
    URI location = buildLocation(createdUser, UserRestController.class);

    log.info("User registered: {}", location);

    return created(location).body(userModelAssembler.toModel(createdUser));
  }

  @Operation(description = "Authenticate a user")
  @ApiResponse(responseCode = "200", description = "User authenticated")
  @ApiResponse(responseCode = "401", description = "Bad credentials")
  @PostMapping(value = SecurityConstants.AUTHENTICATE_ENDPOINT, consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AuthenticateResponse> authenticate(@RequestBody final AuthenticateRequest authRequest)
  {
    // marker method
    // the authentication is done in JwtAuthenticationFilter
    return ok(null);
  }

  @Operation(description = "Refresh a user access token")
  @ApiResponse(responseCode = "200", description = "Access token refreshed")
  @PostMapping(value = SecurityConstants.REFRESH_TOKEN_ENDPOINT, consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AuthenticateResponse> refreshToken(@RequestBody final RefreshTokenRequest request)
      throws UnparsableToken, InvalidTokenCreation, EntityNotFound
  {
    log.debug("Refreshing the access token of a user");

    Authentication authentication = jwtService.parseToken(request.token());
    String username = (String) authentication.getPrincipal();
    UserDTO user = userDtoService.getByUsername(username);

    String accessToken = jwtService.createAccessToken(username,
        user.getRoles()
          .stream()
          .map(SimpleGrantedAuthority::new)
          .toList());

    log.info("Access token refreshed for the user '{}'", authentication.getPrincipal());

    return ok(new AuthenticateResponse(accessToken, request.token()));
  }
}
