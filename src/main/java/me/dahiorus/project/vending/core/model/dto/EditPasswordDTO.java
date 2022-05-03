package me.dahiorus.project.vending.core.model.dto;

import lombok.Getter;
import lombok.Setter;

public class EditPasswordDTO
{
  @Getter
  @Setter
  private String oldPassword;

  @Getter
  @Setter
  private String password;
}
