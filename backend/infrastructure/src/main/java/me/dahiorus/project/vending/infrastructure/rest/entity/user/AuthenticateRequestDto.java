package me.dahiorus.project.vending.infrastructure.rest.entity.user;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;

@Schema(description = "Request to authenticate a user")
public record AuthenticateRequestDto(
    @SchemaProperty(
            name = "username",
            schema =
                @Schema(
                    example = "username",
                    requiredMode = REQUIRED,
                    description = "The username"))
        String username,
    @SchemaProperty(
            name = "password",
            schema =
                @Schema(
                    example = "password",
                    requiredMode = REQUIRED,
                    description = "The password"))
        String password) {
  @Override
  public String toString() {
    return "AuthenticateRequestDto [username=" + username + "]";
  }
}
