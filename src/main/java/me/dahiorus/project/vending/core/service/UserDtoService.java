package me.dahiorus.project.vending.core.service;

import java.util.Optional;
import java.util.UUID;

import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.AppUser;
import me.dahiorus.project.vending.core.model.dto.BinaryDataDTO;
import me.dahiorus.project.vending.core.model.dto.EditPasswordDTO;
import me.dahiorus.project.vending.core.model.dto.UserDTO;
import me.dahiorus.project.vending.core.model.dto.UserWithPasswordDTO;

public interface UserDtoService extends DtoService<AppUser, UserDTO>
{
  UserDTO create(UserWithPasswordDTO user) throws ValidationException;

  UserDTO getByUsername(String username) throws EntityNotFound;

  void updatePassword(UUID id, EditPasswordDTO editPassword) throws EntityNotFound, ValidationException;

  Optional<BinaryDataDTO> getImage(UUID id) throws EntityNotFound;

  UserDTO uploadImage(UUID id, BinaryDataDTO picture) throws EntityNotFound;
}
