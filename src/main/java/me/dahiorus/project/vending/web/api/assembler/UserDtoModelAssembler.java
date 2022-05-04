package me.dahiorus.project.vending.web.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.exception.AppException;
import me.dahiorus.project.vending.core.model.dto.UserDTO;
import me.dahiorus.project.vending.web.api.impl.SelfServiceRestController;
import me.dahiorus.project.vending.web.api.impl.UserRestController;
import me.dahiorus.project.vending.web.security.AuthenticationFacade;

@Log4j2
@RequiredArgsConstructor
@Component
public class UserDtoModelAssembler extends DtoModelAssembler<UserDTO>
{
  private final AuthenticationFacade authenticationFacade;

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

  @Override
  protected Iterable<Link> buildLinks(final UserDTO content) throws AppException
  {
    Collection<Link> links = new LinkedList<>();

    UserDTO authenticatedUser = authenticationFacade.getAuthenticatedUser();

    if (content.equals(authenticatedUser))
    {
      Link selfServiceLink = linkTo(methodOn(SelfServiceRestController.class).get()).withRel("me:get");
      Link updatePassword = linkTo(methodOn(SelfServiceRestController.class).updatePassword(null))
        .withRel("me:update-password");
      links.addAll(List.of(selfServiceLink, updatePassword));
    }

    return links;
  }
}
