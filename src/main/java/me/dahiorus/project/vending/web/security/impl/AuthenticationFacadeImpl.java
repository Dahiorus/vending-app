package me.dahiorus.project.vending.web.security.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.UserNotAuthenticated;
import me.dahiorus.project.vending.core.model.dto.UserDTO;
import me.dahiorus.project.vending.core.service.UserDtoService;
import me.dahiorus.project.vending.web.security.AuthenticationFacade;

@Component
@RequiredArgsConstructor
@Log4j2
public class AuthenticationFacadeImpl implements AuthenticationFacade
{
  private final UserDtoService userDtoService;

  @Override
  public UserDTO getAuthenticatedUser() throws UserNotAuthenticated
  {
    log.debug("Get the authenticated user");

    Authentication authentication = SecurityContextHolder.getContext()
      .getAuthentication();

    try
    {
      return userDtoService.getByUsername(authentication.getName());
    }
    catch (EntityNotFound e)
    {
      throw new UsernameNotFoundException("Unable to get an authenticated user");
    }
  }
}
