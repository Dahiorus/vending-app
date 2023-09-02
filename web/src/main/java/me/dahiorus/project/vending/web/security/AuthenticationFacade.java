package me.dahiorus.project.vending.web.security;

import org.springframework.security.core.Authentication;

import me.dahiorus.project.vending.domain.exception.UserNotAuthenticated;
import me.dahiorus.project.vending.domain.model.dto.UserDto;

public interface AuthenticationFacade
{
  UserDto getAuthenticatedUser(Authentication authentication) throws UserNotAuthenticated;
}
