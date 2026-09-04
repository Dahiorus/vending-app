package me.dahiorus.project.vending.domain.user.entity;

import java.io.Serializable;

public record AdminUser(UserId id, EmailAddress email, Firstname firstname, Lastname lastname)
    implements Serializable {}
