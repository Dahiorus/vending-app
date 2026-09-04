package me.dahiorus.project.vending.domain.stock.entity;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import me.dahiorus.project.vending.domain.item.entity.Item;
import me.dahiorus.project.vending.domain.item.entity.ItemId;

public record VendingMachineStock(Map<ItemId, ItemQuantity> itemQuantities)
    implements Serializable {
  public VendingMachineStock {
    itemQuantities = unmodifiableMap(itemQuantities);
  }

  public VendingMachineStock(Set<ItemQuantity> itemQuantities) {
    this(itemQuantities.stream().collect(toMap(ItemQuantity::itemId, itemQuantity -> itemQuantity)));
  }

  public Stream<ItemQuantity> stream() {
    return itemQuantities.values().stream();
  }

  public boolean hasStock(final Item item) {
    return quantityInStock(item).value() > 0;
  }

  public Optional<ItemQuantity> findStock(final Item item) {
    return Optional.ofNullable(itemQuantities.get(item.id()));
  }

  public Quantity quantityInStock(final Item item) {
    return findStock(item)
        .map(ItemQuantity::quantity)
        .orElseThrow(() -> new IllegalArgumentException("No stock available for item: " + item));
  }

  public VendingMachineStock addStock(ItemQuantity itemQuantity) {
    var updatedStocks = new HashMap<>(itemQuantities);
    findStock(itemQuantity.item())
        .map(ItemQuantity::quantity)
        .ifPresentOrElse(
            currentQuantity ->
                updatedStocks.put(
                    itemQuantity.itemId(),
                    new ItemQuantity(
                        itemQuantity.item(), currentQuantity.add(itemQuantity.quantity()))),
            () -> updatedStocks.put(itemQuantity.itemId(), itemQuantity));

    return new VendingMachineStock(updatedStocks);
  }

  public VendingMachineStock decrementStock(Item item) {
    var current =
        findStock(item)
            .orElseThrow(
                () -> new IllegalArgumentException("No stock available for item: " + item));

    if (current.doesNotHaveStock()) {
      throw new IllegalArgumentException("No stock available for item: " + item);
    }

    var updatedStocks = new HashMap<>(itemQuantities);
    updatedStocks.put(item.id(), current.decrementQuantity());
    return new VendingMachineStock(updatedStocks);
  }
}
