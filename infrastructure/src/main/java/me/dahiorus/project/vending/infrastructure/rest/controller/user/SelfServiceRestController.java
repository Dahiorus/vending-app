package me.dahiorus.project.vending.infrastructure.rest.controller.user;

import static me.dahiorus.project.vending.infrastructure.rest.controller.MultipartFileValidator.validator;
import static me.dahiorus.project.vending.infrastructure.rest.utils.ToFileToUploadConvertor.toFileToUpload;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.user.entity.AppUser;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.port.AppUserApiPort;
import me.dahiorus.project.vending.infrastructure.rest.entity.user.EditPasswordRequestDto;
import me.dahiorus.project.vending.infrastructure.rest.entity.user.UserDto;
import me.dahiorus.project.vending.infrastructure.rest.entity.user.UserToUpdateDto;
import me.dahiorus.project.vending.infrastructure.rest.exception.UserNotAuthenticated;
import me.dahiorus.project.vending.infrastructure.rest.utils.ToByteArrayResponseConvertor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Self-service", description = "Operation on the authenticated user")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping(value = "/api/v1/me")
public class SelfServiceRestController {

  private final AppUserApiPort appUserService;
  private final RepresentationModelAssembler<UserDto, EntityModel<UserDto>> modelAssembler;

  public SelfServiceRestController(
      final AppUserApiPort appUserService,
      final RepresentationModelAssembler<UserDto, EntityModel<UserDto>> modelAssembler) {
    this.appUserService = appUserService;
    this.modelAssembler = modelAssembler;
  }

  @Operation(description = "Get the authenticated user")
  @ApiResponse(responseCode = "200", description = "Authenticated user found")
  @GetMapping(produces = HAL_JSON_VALUE)
  public ResponseEntity<EntityModel<UserDto>> get(Authentication authentication) {
    var authenticatedUser = getAuthenticatedUser(authentication);

    return ok(modelAssembler.toModel(UserDto.fromDomain(authenticatedUser)));
  }

  @Operation(description = "Update self information")
  @ApiResponse(responseCode = "200", description = "Authenticated user updated")
  @PutMapping(produces = HAL_JSON_VALUE)
  public ResponseEntity<EntityModel<UserDto>> update(
      Authentication authentication, UserToUpdateDto userDto) {
    var authenticatedUser = getAuthenticatedUser(authentication);
    var updatedUser = appUserService.update(userDto.toDomain(authenticatedUser.id()));

    return ok(modelAssembler.toModel(UserDto.fromDomain(updatedUser)));
  }

  @Operation(description = "Upload a profile picture to self")
  @ApiResponse(responseCode = "200", description = "Picture uploaded")
  @PostMapping(value = "/picture", consumes = MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<EntityModel<UserDto>> uploadProfilePicture(
      Authentication authentication, @RequestParam("file") MultipartFile multipartFile) {
    var authenticatedUser = getAuthenticatedUser(authentication);

    validator(multipartFile).validate();
    var appUserWithPicture =
        appUserService.uploadProfilePicture(authenticatedUser.id(), toFileToUpload(multipartFile));

    return ok(modelAssembler.toModel(UserDto.fromDomain(appUserWithPicture.user())));
  }

  @Operation(description = "Get the authenticated user's profile picture")
  @ApiResponse(responseCode = "200", description = "User picture found")
  @ApiResponse(responseCode = "404", description = "No picture found")
  @GetMapping("/picture")
  public ResponseEntity<ByteArrayResource> getProfilePicture(Authentication authentication) {
    var authenticatedUser = getAuthenticatedUser(authentication);
    var maybeProfilePicture = appUserService.getProfilePicture(authenticatedUser.id());

    return maybeProfilePicture
        .map(ToByteArrayResponseConvertor::toResponseEntity)
        .orElse(notFound().build());
  }

  @Operation(description = "Update the password of the authenticated user")
  @ApiResponse(responseCode = "204", description = "Password updated")
  @PostMapping(value = "/password", consumes = APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> updatePassword(
      Authentication authentication, @RequestBody EditPasswordRequestDto editPasswordRequestDto) {
    var authenticatedUser = getAuthenticatedUser(authentication);
    appUserService.updateUserPassword(authenticatedUser.id(), editPasswordRequestDto.toDomain());

    return noContent().build();
  }

  private AppUser getAuthenticatedUser(Authentication authentication) {
    return Optional.ofNullable(authentication)
        .map(Authentication::getName)
        .map(EmailAddress::of)
        .map(this::getUserOrThrowNotAuthenticated)
        .orElseThrow(UserNotAuthenticated::new);
  }

  private AppUser getUserOrThrowNotAuthenticated(final EmailAddress email) {
    try {
      return appUserService.getByUsername(email);
    } catch (ResourceNotFound e) {
      throw new UserNotAuthenticated();
    }
  }
}
