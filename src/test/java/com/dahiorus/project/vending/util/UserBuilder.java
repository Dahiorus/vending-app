package com.dahiorus.project.vending.util;

import java.util.UUID;

import me.dahiorus.project.vending.core.model.AppUser;
import me.dahiorus.project.vending.core.model.dto.UserDTO;
import me.dahiorus.project.vending.core.service.DtoMapper;
import me.dahiorus.project.vending.core.service.impl.DtoMapperImpl;

public class UserBuilder
{
  private final DtoMapper dtoMapper = new DtoMapperImpl();

  private AppUser user = new AppUser();

  public static UserBuilder builder()
  {
    return new UserBuilder();
  }

  public UserBuilder id(final UUID id)
  {
    user.setId(id);
    return this;
  }

  public UserBuilder firstName(final String firstName)
  {
    user.setFirstName(firstName);
    return this;
  }

  public UserBuilder lastName(final String lastName)
  {
    user.setLastName(lastName);
    return this;
  }

  public UserBuilder email(final String email)
  {
    user.setEmail(email);
    return this;
  }

  public UserBuilder password(final String password)
  {
    user.setPassword(password);
    return this;
  }

  public AppUser build()
  {
    return user;
  }

  public <D extends UserDTO> D buildDto(final Class<D> dtoClass)
  {
    return dtoMapper.toDto(user, dtoClass);
  }
}
