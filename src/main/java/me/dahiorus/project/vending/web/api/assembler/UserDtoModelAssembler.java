package me.dahiorus.project.vending.web.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.Link;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.exception.AppException;
import me.dahiorus.project.vending.domain.exception.UserNotAuthenticated;
import me.dahiorus.project.vending.domain.model.dto.EditPasswordDTO;
import me.dahiorus.project.vending.domain.model.dto.UserDTO;
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

    try
    {
      Authentication authentication = SecurityContextHolder.getContext()
        .getAuthentication();
      UserDTO authenticatedUser = authenticationFacade.getAuthenticatedUser(authentication);
      if (content.equals(authenticatedUser))
      {
        Link selfServiceLink = linkTo(methodOn(SelfServiceRestController.class).get(null)).withRel("me:get");
        Link updatePassword = linkTo(
            methodOn(SelfServiceRestController.class).updatePassword(null, new EditPasswordDTO(null, null)))
              .withRel("me:update-password");
        Link pictureLink = linkTo(methodOn(SelfServiceRestController.class).getPicture(null)).withRel("me:picture");
        links.addAll(List.of(selfServiceLink, updatePassword, pictureLink));
      }
    }
    catch (UserNotAuthenticated e)
    {
      log.debug(e.getMessage());
    }

    return links;
  }
}
