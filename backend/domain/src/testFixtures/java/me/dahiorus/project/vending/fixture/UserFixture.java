package me.dahiorus.project.vending.fixture;

import static java.util.UUID.randomUUID;

import me.dahiorus.project.vending.domain.user.entity.AdminUser;
import me.dahiorus.project.vending.domain.user.entity.AppUser;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.Firstname;
import me.dahiorus.project.vending.domain.user.entity.Lastname;
import me.dahiorus.project.vending.domain.user.entity.UserId;

public class UserFixture {

  public static Builder aUser() {
    return new Builder()
        .id(new UserId(randomUUID()))
        .emailAddress(EmailAddress.of("user@test.org"))
        .firstname(Firstname.of("User"))
        .lastname(Lastname.of("Test"));
  }

  public static class Builder {
    private UserId id;
    private EmailAddress emailAddress;
    private Firstname firstname;
    private Lastname lastname;

    public Builder id(UserId userId) {
      this.id = userId;
      return this;
    }

    public Builder emailAddress(EmailAddress emailAddress) {
      this.emailAddress = emailAddress;
      return this;
    }

    public Builder firstname(Firstname firstname) {
      this.firstname = firstname;
      return this;
    }

    public Builder lastname(Lastname lastname) {
      this.lastname = lastname;
      return this;
    }

    public AdminUser buildAdmin() {
      return new AdminUser(id, emailAddress, firstname, lastname);
    }

    public AppUser buildUser() {
      return new AppUser(id, emailAddress, firstname, lastname);
    }
  }
}
