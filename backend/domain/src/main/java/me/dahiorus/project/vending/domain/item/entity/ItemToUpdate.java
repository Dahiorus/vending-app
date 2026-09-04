package me.dahiorus.project.vending.domain.item.entity;

import java.math.BigDecimal;

public record ItemToUpdate(ItemId id, BigDecimal price) {}
