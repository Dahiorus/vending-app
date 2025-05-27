package me.dahiorus.project.vending.infrastructure.rest.entity.user;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import me.dahiorus.project.vending.domain.user.entity.EditPassword;
import me.dahiorus.project.vending.domain.user.entity.Password;

@Schema(description = "Request to update a user's password")
public record EditPasswordRequestDto(
    @SchemaProperty(
            name = "oldPassword",
            schema = @Schema(requiredMode = REQUIRED, description = "The user's current password"))
        String oldPassword,
    @SchemaProperty(
            name = "newPassword",
            schema = @Schema(requiredMode = REQUIRED, description = "The user's new password"))
        String newPassword) {

  public EditPassword toDomain() {
    return new EditPassword(Password.of(oldPassword), Password.of(newPassword));
  }

  @Override
  public String toString() {
    return "EditPasswordRequestDto{ oldPassword='******', newPassword='*******' }";
  }
}
