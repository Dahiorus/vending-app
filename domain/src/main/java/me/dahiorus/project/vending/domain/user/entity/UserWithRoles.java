package me.dahiorus.project.vending.domain.user.entity;

import java.io.Serializable;
import java.util.Set;

public record UserWithRoles(UserId id, EmailAddress username, Set<Role> roles)
    implements Serializable {}
