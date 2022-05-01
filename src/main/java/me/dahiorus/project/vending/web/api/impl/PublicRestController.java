package me.dahiorus.project.vending.web.api.impl;

import static org.springframework.http.ResponseEntity.created;

import java.net.URI;

import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.common.HasLogger;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.dto.UserDTO;
import me.dahiorus.project.vending.core.model.dto.UserWithPasswordDTO;
import me.dahiorus.project.vending.core.service.UserDtoService;
import me.dahiorus.project.vending.web.api.AppWebService;

@Tag(name = "Public", description = "Public operations")
@RestController
@Log4j2
@AllArgsConstructor
public class PublicRestController implements HasLogger, AppWebService
{
  private final UserDtoService userDtoService;

  private final RepresentationModelAssembler<UserDTO, EntityModel<UserDTO>> userModelAssembler;

  @Override
  public Logger getLogger()
  {
    return log;
  }

  @Operation(description = "Register a user")
  @ApiResponse(responseCode = "201", description = "User registered")
  @PostMapping(value = "/api/v1/register", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaTypes.HAL_JSON_VALUE)
  public ResponseEntity<EntityModel<UserDTO>> register(@RequestBody final UserWithPasswordDTO user)
      throws ValidationException
  {
    log.debug("Signing up a new user");
    UserDTO createdUser = userDtoService.create(user);

    URI location = MvcUriComponentsBuilder.fromController(UserRestController.class)
      .path("/{id}")
      .buildAndExpand(createdUser.getId())
      .toUri();

    log.info("User registered: {}", location);

    return created(location).body(userModelAssembler.toModel(createdUser));
  }
}
