package me.dahiorus.project.vending.domain.user.entity;

import java.io.Serializable;
import java.util.Set;

public record UserWithRoles(
    UserId id, EmailAddress username, Password encodedPassword, Set<Role> roles)
    implements Serializable {
  public String[] rolesAsStringArray() {
    return roles.stream().map(Role::value).toArray(String[]::new);
  }
}
