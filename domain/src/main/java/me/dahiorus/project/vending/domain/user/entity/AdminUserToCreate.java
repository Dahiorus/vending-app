package me.dahiorus.project.vending.domain.user.entity;

public record AdminUserToCreate(
    EmailAddress emailAddress, Password password, Firstname firstname, Lastname lastname)
    implements UserToCreate {}
