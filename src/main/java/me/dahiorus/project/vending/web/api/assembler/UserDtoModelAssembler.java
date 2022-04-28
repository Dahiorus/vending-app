package me.dahiorus.project.vending.web.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import me.dahiorus.project.vending.core.exception.AppException;
import me.dahiorus.project.vending.core.model.dto.UserDTO;
import me.dahiorus.project.vending.web.api.impl.UserRestController;

@Component
public class UserDtoModelAssembler extends DtoModelAssembler<UserDTO>
{
  private static final Logger logger = LogManager.getLogger(UserDtoModelAssembler.class);

  @Override
  public Logger getLogger()
  {
    return logger;
  }

  @Override
  protected Optional<Link> selfLink(final UserDTO content) throws AppException
  {
    Link selfLink = linkTo(methodOn(UserRestController.class).read(content.getId())).withSelfRel();
    return Optional.of(selfLink);
  }
}
