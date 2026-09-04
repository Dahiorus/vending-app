package me.dahiorus.project.vending.infrastructure.rest.entity.user;

import java.util.UUID;
import me.dahiorus.project.vending.domain.user.entity.AppUser;

public record UserDto(UUID id, String email, String firstname, String lastname) {
  public static UserDto fromDomain(AppUser user) {
    return new UserDto(
        user.id().value(), user.email().value(), user.firstname().value(), user.lastname().value());
  }
}
