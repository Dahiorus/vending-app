package me.dahiorus.project.vending.web.security;

import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.model.dto.UserDTO;

public interface AuthenticationFacade
{
  UserDTO getAuthenticatedUser() throws EntityNotFound;
}
