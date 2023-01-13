package me.dahiorus.project.vending.web.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;

@Schema(description = "Request to authenticate a user")
@SchemaProperty(name = "username",
  schema = @Schema(example = "username", required = true, description = "The username"))
@SchemaProperty(name = "password",
  schema = @Schema(example = "password", required = true, description = "The password"))
public record AuthenticateRequest(String username, String password)
{
  @Override
  public String toString()
  {
    return "AuthenticateRequest [username=" + username + "]";
  }
}
