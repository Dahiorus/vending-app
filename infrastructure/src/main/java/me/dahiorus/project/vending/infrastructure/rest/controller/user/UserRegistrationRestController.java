package me.dahiorus.project.vending.infrastructure.rest.controller.user;

import static org.springframework.hateoas.IanaLinkRelations.SELF;
import static org.springframework.hateoas.MediaTypes.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.http.ResponseEntity.created;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.dahiorus.project.vending.domain.exception.InvalidBusinessObject;
import me.dahiorus.project.vending.domain.user.port.AppUserApiPort;
import me.dahiorus.project.vending.infrastructure.rest.entity.user.UserDto;
import me.dahiorus.project.vending.infrastructure.rest.entity.user.UserToRegisterDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public")
@RestController
@RequestMapping("/api/v1/register")
public class UserRegistrationRestController {

  private final AppUserApiPort appUserService;
  private final RepresentationModelAssembler<UserDto, EntityModel<UserDto>> modelAssembler;

  public UserRegistrationRestController(
      final AppUserApiPort appUserService,
      final RepresentationModelAssembler<UserDto, EntityModel<UserDto>> modelAssembler) {
    this.appUserService = appUserService;
    this.modelAssembler = modelAssembler;
  }

  @Operation(description = "Register a user")
  @ApiResponse(responseCode = "201", description = "User registered")
  @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = HAL_JSON_VALUE)
  public ResponseEntity<EntityModel<UserDto>> create(
      @Valid @RequestBody final UserToRegisterDto user) throws InvalidBusinessObject {

    var createdUser = appUserService.create(user.toCreate());
    var userModel = modelAssembler.toModel(UserDto.fromDomain(createdUser));

    return created(userModel.getRequiredLink(SELF).toUri()).body(userModel);
  }
}
