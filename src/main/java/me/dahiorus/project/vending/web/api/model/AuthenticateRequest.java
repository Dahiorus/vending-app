package me.dahiorus.project.vending.web.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to authenticate a user")
public record AuthenticateRequest(String username, String password)
{
  @Override
  public String toString()
  {
    return "AuthenticateRequest [username=" + username + "]";
  }
}
