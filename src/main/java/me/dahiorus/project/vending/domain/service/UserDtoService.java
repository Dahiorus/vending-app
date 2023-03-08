package me.dahiorus.project.vending.domain.service;

import java.util.UUID;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.AppUser;
import me.dahiorus.project.vending.domain.model.dto.EditPasswordDTO;
import me.dahiorus.project.vending.domain.model.dto.UserDTO;

public interface UserDtoService extends DtoService<AppUser, UserDTO>, BinaryDataDtoService<UserDTO>
{
  UserDTO getByUsername(String username) throws EntityNotFound;

  void updatePassword(UUID id, EditPasswordDTO editPassword) throws EntityNotFound, ValidationException;
}
