package me.dahiorus.project.vending.domain.model.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.dahiorus.project.vending.domain.model.AppUser;

@Relation(collectionRelation = "elements")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = { "password" })
public class UserDto extends AbstractDto<AppUser>
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

  @Parameter(hidden = true)
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
