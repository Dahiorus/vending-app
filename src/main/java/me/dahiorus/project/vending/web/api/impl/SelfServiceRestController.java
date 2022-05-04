package me.dahiorus.project.vending.web.api.impl;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.UserNotAuthenticated;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.dto.EditPasswordDTO;
import me.dahiorus.project.vending.core.model.dto.UserDTO;
import me.dahiorus.project.vending.core.service.UserDtoService;
import me.dahiorus.project.vending.web.api.AppWebService;
import me.dahiorus.project.vending.web.security.AuthenticationFacade;

@Tag(name = "Self-service", description = "Operation on the authenticated user")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping(value = "/api/v1/me", produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
@Log4j2
public class SelfServiceRestController implements AppWebService
{
  private final AuthenticationFacade authenticationFacade;

  private final UserDtoService userDtoService;

  private final RepresentationModelAssembler<UserDTO, EntityModel<UserDTO>> userModelAssembler;

  @Operation(description = "Get the authenticated user")
  @ApiResponse(responseCode = "200", description = "Authenticated user found")
  @GetMapping
  public ResponseEntity<EntityModel<UserDTO>> get() throws UserNotAuthenticated
  {
    UserDTO authenticatedUser = authenticationFacade.getAuthenticatedUser();

    log.info("Got authenticated user {}", authenticatedUser);

    return ResponseEntity.ok(userModelAssembler.toModel(authenticatedUser));
  }

  @Operation(description = "Update the password of the authenticated user")
  @ApiResponse(responseCode = "200", description = "Password updated")
  @PostMapping("/password")
  public ResponseEntity<Void> updatePassword(@RequestBody final EditPasswordDTO editPassword)
      throws ValidationException, UserNotAuthenticated
  {
    UserDTO authenticatedUser = authenticationFacade.getAuthenticatedUser();

    try
    {
      userDtoService.updatePassword(authenticatedUser.getId(), editPassword);
    }
    catch (EntityNotFound e)
    {
      throw new UserNotAuthenticated("Unable to get the autenticated user");
    }

    log.info("Updated password of {}", authenticatedUser);

    return ResponseEntity.noContent()
      .build();
  }
}
