package me.dahiorus.project.vending.domain.item.entity;

import java.util.UUID;
import me.dahiorus.project.vending.domain.DomainId;

public record ItemId(UUID value) implements DomainId {}
