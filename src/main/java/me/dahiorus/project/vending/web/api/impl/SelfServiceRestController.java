package me.dahiorus.project.vending.web.api.impl;

import static org.springframework.http.ResponseEntity.ok;

import java.io.IOException;
import java.util.Optional;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.exception.AppRuntimeException;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.UserNotAuthenticated;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.dto.BinaryDataDTO;
import me.dahiorus.project.vending.core.model.dto.EditPasswordDTO;
import me.dahiorus.project.vending.core.model.dto.UserDTO;
import me.dahiorus.project.vending.core.service.UserDtoService;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;
import me.dahiorus.project.vending.web.api.AppWebService;
import me.dahiorus.project.vending.web.api.request.MultiPartFileUtils;
import me.dahiorus.project.vending.web.security.AuthenticationFacade;

@Tag(name = "Self-service", description = "Operation on the authenticated user")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping(value = "/api/v1/me")
@RequiredArgsConstructor
@Log4j2
public class SelfServiceRestController implements AppWebService
{
  private final AuthenticationFacade authenticationFacade;

  private final UserDtoService userDtoService;

  private final RepresentationModelAssembler<UserDTO, EntityModel<UserDTO>> userModelAssembler;

  @Operation(description = "Get the authenticated user")
  @ApiResponse(responseCode = "200", description = "Authenticated user found")
  @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
  public ResponseEntity<EntityModel<UserDTO>> get() throws UserNotAuthenticated
  {
    UserDTO authenticatedUser = authenticationFacade.getAuthenticatedUser();

    log.info("Got authenticated user {}", authenticatedUser);

    return ResponseEntity.ok(userModelAssembler.toModel(authenticatedUser));
  }

  @Operation(description = "Update the password of the authenticated user")
  @ApiResponse(responseCode = "204", description = "Password updated")
  @PostMapping(value = "/password", consumes = MediaType.APPLICATION_JSON_VALUE)
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

  @Operation(description = "Upload a profile picture to the authenticated user")
  @ApiResponse(responseCode = "200", description = "Picture uploaded")
  @PostMapping(value = "picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<EntityModel<UserDTO>> uploadPicture(@RequestParam("file") final MultipartFile file)
      throws UserNotAuthenticated, ValidationException
  {
    UserDTO authenticatedUser = authenticationFacade.getAuthenticatedUser();

    ValidationResults validationResults = MultiPartFileUtils.validateImage("file", file);
    validationResults.throwIfError("Unable to upload the given file");

    try
    {
      BinaryDataDTO picture = MultiPartFileUtils.convert(file);
      UserDTO updatedUser = userDtoService.uploadImage(authenticatedUser.getId(), picture);

      return ok(userModelAssembler.toModel(updatedUser));
    }
    catch (IOException e)
    {
      throw new AppRuntimeException("Unable to get the file to upload", e);
    }
    catch (EntityNotFound e)
    {
      throw new UserNotAuthenticated("User not authenticated");
    }
  }

  @Operation(description = "Get the authenticated user's profile picture")
  @ApiResponse(responseCode = "200", description = "User picture found")
  @ApiResponse(responseCode = "404", description = "No picture found")
  @GetMapping("/picture")
  public ResponseEntity<ByteArrayResource> getPicture() throws UserNotAuthenticated
  {
    UserDTO authenticatedUser = authenticationFacade.getAuthenticatedUser();
    try
    {
      Optional<BinaryDataDTO> picture = userDtoService.getImage(authenticatedUser.getId());

      log.info("Picture for the authenticated user: {}", picture);

      return MultiPartFileUtils.convertToResponse(picture);
    }
    catch (EntityNotFound e)
    {
      throw new UserNotAuthenticated("User not authenticated");
    }
  }
}
