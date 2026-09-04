package me.dahiorus.project.vending.domain.user.port;

import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.UserWithRoles;

public interface UserWithRolesRepositoryPort {
  UserWithRoles getByUsername(EmailAddress emailAddress) throws ResourceNotFound;
}
