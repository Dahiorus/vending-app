package me.dahiorus.project.vending.util;

import java.util.List;
import java.util.UUID;

import me.dahiorus.project.vending.core.model.AppUser;
import me.dahiorus.project.vending.core.model.BinaryData;
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

  public UserBuilder encodedPassword(final String encodedPassword)
  {
    user.setEncodedPassword(encodedPassword);
    return this;
  }

  public UserBuilder password(final String password)
  {
    user.setPassword(password);
    return this;
  }

  public UserBuilder picture(final String name, final String contentType)
  {
    BinaryData binaryData = new BinaryData();
    binaryData.setName(name);
    binaryData.setContentType(contentType);
    binaryData.setContent(new byte[0]);
    user.setPicture(binaryData);

    return this;
  }

  public UserBuilder roles(final List<String> roles)
  {
    user.setRoles(roles);
    return this;
  }

  public AppUser build()
  {
    return user;
  }

  public UserDTO buildDto()
  {
    return dtoMapper.toDto(user, UserDTO.class);
  }
}
