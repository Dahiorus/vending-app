package me.dahiorus.project.vending.domain.machine.entity;

import java.time.LocalDateTime;

public record VendingMachineToUpdate(
    VendingMachineId id,
    Address address,
    VendingMachineStatus status,
    LocalDateTime lastIntervention) {}
