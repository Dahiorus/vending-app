package me.dahiorus.project.vending.domain.service;

import java.util.UUID;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.AppUser;
import me.dahiorus.project.vending.domain.model.dto.EditPasswordDto;
import me.dahiorus.project.vending.domain.model.dto.UserDto;

public interface UserDtoService extends DtoService<AppUser, UserDto>, BinaryDataDtoService<UserDto>
{
  UserDto getByUsername(String username) throws EntityNotFound;

  void updatePassword(UUID id, EditPasswordDto editPassword) throws EntityNotFound, ValidationException;
}
