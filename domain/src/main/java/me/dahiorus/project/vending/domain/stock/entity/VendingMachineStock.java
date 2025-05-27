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

public record VendingMachineStock(Map<Item, Quantity> itemQuantities) implements Serializable {
  public VendingMachineStock {
    itemQuantities = unmodifiableMap(itemQuantities);
  }

  public VendingMachineStock(Set<ItemQuantity> itemQuantities) {
    this(itemQuantities.stream().collect(toMap(ItemQuantity::item, ItemQuantity::quantity)));
  }

  public Stream<ItemQuantity> stream() {
    return itemQuantities.entrySet().stream()
        .map(entry -> new ItemQuantity(entry.getKey(), entry.getValue()));
  }

  public boolean hasStock(final Item item) {
    return quantityInStock(item).value() > 0;
  }

  public Optional<ItemQuantity> findStock(final Item item) {
    return Optional.ofNullable(itemQuantities.get(item))
        .map(quantity -> new ItemQuantity(item, quantity));
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
                    itemQuantity.item(), currentQuantity.add(itemQuantity.quantity())),
            () -> updatedStocks.put(itemQuantity.item(), itemQuantity.quantity()));

    return new VendingMachineStock(updatedStocks);
  }

  public VendingMachineStock decrementStock(Item item) {
    return findStock(item)
        .map(ItemQuantity::quantity)
        .map(
            currentQuantity -> {
              var updatedStocks = new HashMap<>(itemQuantities);
              updatedStocks.put(item, currentQuantity.decrement());
              return new VendingMachineStock(updatedStocks);
            })
        .orElseThrow(() -> new IllegalArgumentException("No stock available for item: " + item));
  }
}
