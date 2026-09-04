package me.dahiorus.project.vending.domain.user.entity;

public sealed interface UserToCreate permits AppUserToCreate, AdminUserToCreate {
  EmailAddress emailAddress();

  Password password();

  Firstname firstname();

  Lastname lastname();
}
