package me.dahiorus.project.vending.core.model.dto;

public record EditPasswordDTO(String oldPassword, String password)
{
  @Override
  public String toString()
  {
    return "EditPasswordDTO []";
  }
}
