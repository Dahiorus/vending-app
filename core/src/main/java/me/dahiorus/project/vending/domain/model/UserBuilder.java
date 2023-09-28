package me.dahiorus.project.vending.domain.model;

import java.util.List;
import java.util.UUID;

import me.dahiorus.project.vending.domain.model.dto.UserDto;

public class UserBuilder
{
  private AppUser user = new AppUser();
  private UserDto dto = new UserDto();

  public static UserBuilder builder()
  {
    return new UserBuilder();
  }

  public UserBuilder id(final UUID id)
  {
    user.setId(id);
    dto.setId(id);
    return this;
  }

  public UserBuilder firstName(final String firstName)
  {
    user.setFirstName(firstName);
    dto.setFirstName(firstName);
    return this;
  }

  public UserBuilder lastName(final String lastName)
  {
    user.setLastName(lastName);
    dto.setLastName(lastName);
    return this;
  }

  public UserBuilder email(final String email)
  {
    user.setEmail(email);
    dto.setEmail(email);
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
    dto.setPassword(password);
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
    dto.setRoles(roles);
    return this;
  }

  public AppUser build()
  {
    return user;
  }

  public UserDto buildDto()
  {
    return dto;
  }
}
