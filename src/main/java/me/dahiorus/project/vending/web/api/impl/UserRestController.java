package me.dahiorus.project.vending.web.api.impl;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.dto.UserDTO;
import me.dahiorus.project.vending.domain.service.UserDtoService;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User", description = "Operations on User")
@RestController
@RequestMapping(value = "/api/v1/users")
@Log4j2
public class UserRestController extends RestControllerImpl<UserDTO, UserDtoService>
{
  public UserRestController(final UserDtoService dtoService,
      final RepresentationModelAssembler<UserDTO, EntityModel<UserDTO>> modelAssembler,
      final PagedResourcesAssembler<UserDTO> pageModelAssembler)
  {
    super(dtoService, modelAssembler, pageModelAssembler);
  }

  @Operation(description = "Create a new admin user")
  @Override
  public ResponseEntity<EntityModel<UserDTO>> create(@RequestBody final UserDTO dto) throws ValidationException
  {
    log.debug("Create a new admin: {}", dto);
    dto.setRoles(List.of("ROLE_ADMIN"));

    return super.create(dto);
  }

  @Override
  public Logger getLogger()
  {
    return log;
  }
}
