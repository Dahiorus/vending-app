package me.dahiorus.project.vending.domain.item.entity;

import java.math.BigDecimal;

public record ItemToCreate(ItemName name, ItemType type, BigDecimal price) {}
