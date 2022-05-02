package me.dahiorus.project.vending.web.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Schema(description = "Request to authenticate a user")
@ToString(of = "username")
public class AuthenticateRequest
{
  @Getter
  @Setter
  private String username;

  @Getter
  @Setter
  private String password;
}
