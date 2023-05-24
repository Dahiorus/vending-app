package me.dahiorus.project.vending.web.security.impl;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.UserNotAuthenticated;
import me.dahiorus.project.vending.domain.model.dto.UserDTO;
import me.dahiorus.project.vending.domain.service.UserDtoService;
import me.dahiorus.project.vending.web.security.AuthenticationFacade;

@Component
@RequiredArgsConstructor
@Log4j2
public class AuthenticationFacadeImpl implements AuthenticationFacade
{
  private final UserDtoService userDtoService;

  @Override
  public UserDTO getAuthenticatedUser(final Authentication authentication) throws UserNotAuthenticated
  {
    log.debug("Get the authenticated user");

    if (authentication == null)
    {
      throw userNotAuthenticated();
    }

    try
    {
      return userDtoService.getByUsername(authentication.getName());
    }
    catch (EntityNotFound e)
    {
      throw userNotAuthenticated();
    }
  }

  private static UserNotAuthenticated userNotAuthenticated()
  {
    return new UserNotAuthenticated("Unable to get an authenticated user");
  }
}
