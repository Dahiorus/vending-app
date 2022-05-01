package me.dahiorus.project.vending.core.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class UserWithPasswordDTO extends UserDTO
{
  @Schema(required = true)
  @Getter
  @Setter
  private String password;
}
