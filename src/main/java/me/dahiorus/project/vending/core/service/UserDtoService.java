package me.dahiorus.project.vending.core.service;

import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.User;
import me.dahiorus.project.vending.core.model.dto.UserDTO;
import me.dahiorus.project.vending.core.model.dto.UserWithPasswordDTO;

public interface UserDtoService extends DtoService<User, UserDTO>
{
  UserDTO create(UserWithPasswordDTO user) throws ValidationException;
}
