package me.dahiorus.project.vending.infrastructure.rest.entity.item;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.entity.ItemToUpdate;

public record ItemToUpdateDto(@Positive BigDecimal price) {
  public ItemToUpdate toDomain(UUID id) {
    return new ItemToUpdate(new ItemId(id), price);
  }
}
