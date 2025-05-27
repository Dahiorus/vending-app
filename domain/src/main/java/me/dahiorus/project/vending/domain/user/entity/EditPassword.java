package me.dahiorus.project.vending.domain.user.entity;

public record EditPassword(Password oldPassword, Password newPassword) {
  public boolean isSame() {
    return oldPassword.equals(newPassword);
  }
}
