package me.dahiorus.project.vending.web.api.impl;

import org.apache.logging.log4j.Logger;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.model.dto.UserDTO;
import me.dahiorus.project.vending.core.service.UserDtoService;

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

  @Override
  public Logger getLogger()
  {
    return log;
  }
}
