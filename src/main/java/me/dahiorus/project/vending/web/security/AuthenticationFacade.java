package me.dahiorus.project.vending.web.security;

import org.springframework.security.core.Authentication;

import me.dahiorus.project.vending.core.exception.UserNotAuthenticated;
import me.dahiorus.project.vending.core.model.dto.UserDTO;

public interface AuthenticationFacade
{
  UserDTO getAuthenticatedUser(Authentication authentication) throws UserNotAuthenticated;
}
