package me.dahiorus.project.vending.domain.user.entity;

import java.io.Serializable;

public record AppUser(UserId id, EmailAddress email, Firstname firstname, Lastname lastname)
    implements Serializable {}
