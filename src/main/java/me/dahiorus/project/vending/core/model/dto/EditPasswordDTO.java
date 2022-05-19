package me.dahiorus.project.vending.core.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;

@Schema(description = "Request to edit a user's password", accessMode = AccessMode.WRITE_ONLY)
public record EditPasswordDTO(String oldPassword, String password)
{
  @Override
  public String toString()
  {
    return "EditPasswordDTO []";
  }
}
