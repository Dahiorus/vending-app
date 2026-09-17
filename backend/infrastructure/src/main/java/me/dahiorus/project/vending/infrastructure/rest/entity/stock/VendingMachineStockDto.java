package me.dahiorus.project.vending.infrastructure.rest.entity.stock;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.LinkedHashSet;
import java.util.SequencedSet;
import java.util.UUID;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.stock.entity.ItemQuantity;
import me.dahiorus.project.vending.domain.stock.entity.VendingMachineStock;

public record VendingMachineStockDto(
    @JsonIgnore UUID vendingMachineId, SequencedSet<ItemQuantityDto> itemQuantities) {

  public static VendingMachineStockDto fromDomain(
      VendingMachineId vendingMachineId, VendingMachineStock vendingMachineStock) {
    return new VendingMachineStockDto(
        vendingMachineId.value(),
        vendingMachineStock.itemQuantities().values().stream()
            .map(ItemQuantityDto::fromDomain)
            .sorted(comparing(ItemQuantityDto::itemName))
            .collect(toCollection(LinkedHashSet::new)));
  }

  public record ItemQuantityDto(@JsonIgnore UUID itemId, String itemName, Integer quantity) {
    public static ItemQuantityDto fromDomain(ItemQuantity itemQuantity) {
      return new ItemQuantityDto(
          itemQuantity.itemId().value(),
          itemQuantity.itemName().value(),
          itemQuantity.quantityValue());
    }
  }
}
