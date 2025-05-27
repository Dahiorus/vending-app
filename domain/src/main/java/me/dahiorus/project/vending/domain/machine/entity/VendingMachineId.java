package me.dahiorus.project.vending.domain.machine.entity;

import java.util.UUID;
import me.dahiorus.project.vending.domain.DomainId;

public record VendingMachineId(UUID value) implements DomainId {}
