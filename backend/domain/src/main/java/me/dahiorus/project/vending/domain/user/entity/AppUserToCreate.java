package me.dahiorus.project.vending.domain.user.entity;

public record AppUserToCreate(
    EmailAddress emailAddress, Password password, Firstname firstname, Lastname lastname)
    implements UserToCreate {}
