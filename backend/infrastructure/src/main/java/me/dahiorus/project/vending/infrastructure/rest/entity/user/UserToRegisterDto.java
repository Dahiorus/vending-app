package me.dahiorus.project.vending.infrastructure.rest.entity.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.dahiorus.project.vending.domain.user.entity.AppUserToCreate;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.Firstname;
import me.dahiorus.project.vending.domain.user.entity.Lastname;
import me.dahiorus.project.vending.domain.user.entity.Password;

public record UserToRegisterDto(
    @Email @NotBlank String email,
    @NotNull String password,
    @NotBlank String firstname,
    @NotBlank String lastname) {
  public AppUserToCreate toCreate() {
    return new AppUserToCreate(
        EmailAddress.of(email),
        Password.of(password),
        Firstname.of(firstname),
        Lastname.of(lastname));
  }
}
