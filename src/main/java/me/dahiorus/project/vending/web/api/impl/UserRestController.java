package me.dahiorus.project.vending.web.api.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import me.dahiorus.project.vending.core.model.dto.UserDTO;
import me.dahiorus.project.vending.core.service.UserDtoService;

@Tag(name = "User", description = "Operations on User")
@RestController
@RequestMapping(value = "/v1/users")
public class UserRestController extends RestControllerImpl<UserDTO, UserDtoService>
{
  private static final Logger logger = LogManager.getLogger(UserRestController.class);

  public UserRestController(final UserDtoService dtoService,
      final RepresentationModelAssembler<UserDTO, EntityModel<UserDTO>> modelAssembler,
      final PagedResourcesAssembler<UserDTO> pageModelAssembler)
  {
    super(dtoService, modelAssembler, pageModelAssembler);
  }

  @Override
  public Logger getLogger()
  {
    return logger;
  }
}
