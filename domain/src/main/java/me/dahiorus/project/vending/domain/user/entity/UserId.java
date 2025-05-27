package me.dahiorus.project.vending.domain.user.entity;

import java.util.UUID;
import me.dahiorus.project.vending.domain.DomainId;

public record UserId(UUID value) implements DomainId {}
