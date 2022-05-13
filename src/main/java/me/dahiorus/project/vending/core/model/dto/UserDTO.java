package me.dahiorus.project.vending.core.model.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.dahiorus.project.vending.core.model.AppUser;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = { "password" })
public class UserDTO extends AbstractDTO<AppUser>
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

  @JsonProperty(access = Access.WRITE_ONLY)
  @Getter
  @Setter
  private String password;

  @JsonIgnore
  @Getter
  @Setter
  private UUID pictureId;

  @JsonIgnore
  @Getter
  @Setter
  private List<String> roles = new ArrayList<>();

  @Override
  public Class<AppUser> getEntityClass()
  {
    return AppUser.class;
  }
}
