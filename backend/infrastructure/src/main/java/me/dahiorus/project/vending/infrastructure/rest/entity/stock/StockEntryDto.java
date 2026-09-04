package me.dahiorus.project.vending.infrastructure.rest.entity.stock;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.UUID;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.stock.entity.ItemQuantity;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "elements")
public record StockEntryDto(
    @JsonIgnore UUID vendingMachineId, @JsonIgnore UUID itemId, Integer quantity) {
  public static StockEntryDto fromDomain(
      VendingMachineId vendingMachineId, ItemQuantity itemQuantity) {
    return new StockEntryDto(
        vendingMachineId.value(), itemQuantity.itemId().value(), itemQuantity.quantityValue());
  }
}
