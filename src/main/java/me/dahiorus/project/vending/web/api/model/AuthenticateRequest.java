package me.dahiorus.project.vending.web.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import io.swagger.v3.oas.annotations.media.SchemaProperty;

@Schema(description = "Request to authenticate a user")
@SchemaProperty(name = "username",
  schema = @Schema(example = "username", requiredMode = RequiredMode.REQUIRED, description = "The username"))
@SchemaProperty(name = "password",
  schema = @Schema(example = "password", requiredMode = RequiredMode.REQUIRED, description = "The password"))
public record AuthenticateRequest(String username, String password)
{
  @Override
  public String toString()
  {
    return "AuthenticateRequest [username=" + username + "]";
  }
}
