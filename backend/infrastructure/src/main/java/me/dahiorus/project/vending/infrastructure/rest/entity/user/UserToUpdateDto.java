package me.dahiorus.project.vending.infrastructure.rest.entity.user;

import me.dahiorus.project.vending.domain.user.entity.AppUser;
import me.dahiorus.project.vending.domain.user.entity.Firstname;
import me.dahiorus.project.vending.domain.user.entity.Lastname;
import me.dahiorus.project.vending.domain.user.entity.UserId;

public record UserToUpdateDto(String firstname, String lastname) {
  public AppUser toDomain(UserId id) {
    return new AppUser(id, null, Firstname.of(firstname), Lastname.of(lastname));
  }
}
