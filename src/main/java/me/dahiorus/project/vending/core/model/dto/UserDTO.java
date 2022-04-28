package me.dahiorus.project.vending.core.model.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.dahiorus.project.vending.core.model.User;

@EqualsAndHashCode(callSuper = true)
@ToString
public class UserDTO extends AbstractDTO<User>
{
  @Getter
  @Setter
  private String firstName;

  @Getter
  @Setter
  private String lastName;

  @Getter
  @Setter
  private String email;

  @Override
  public Class<User> getEntityClass()
  {
    return User.class;
  }
}
