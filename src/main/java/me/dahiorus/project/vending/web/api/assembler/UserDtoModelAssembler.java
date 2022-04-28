package me.dahiorus.project.vending.web.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Optional;

import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.exception.AppException;
import me.dahiorus.project.vending.core.model.dto.UserDTO;
import me.dahiorus.project.vending.web.api.impl.UserRestController;

@Log4j2
@Component
public class UserDtoModelAssembler extends DtoModelAssembler<UserDTO>
{
  @Override
  public Logger getLogger()
  {
    return log;
  }

  @Override
  protected Optional<Link> selfLink(final UserDTO content) throws AppException
  {
    Link selfLink = linkTo(methodOn(UserRestController.class).read(content.getId())).withSelfRel();
    return Optional.of(selfLink);
  }
}
