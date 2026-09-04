package me.dahiorus.project.vending.domain.stock.entity;

import static me.dahiorus.project.vending.domain.stock.entity.Quantity.empty;
import static me.dahiorus.project.vending.fixture.ItemFixture.aColdBeverage;
import static me.dahiorus.project.vending.fixture.ItemFixture.anItem;
import static me.dahiorus.project.vending.fixture.ItemQuantityFixture.itemQuantity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class VendingMachineStockTest {
  @Nested
  class HasStock {
    @Test
    void should_have_stock_of_given_item() {
      var coke = aColdBeverage("Coke", 1.5);
      var machineStock = new VendingMachineStock(Set.of(itemQuantity(coke, 5)));

      assertThat(machineStock.hasStock(coke)).isTrue();
    }

    @Test
    void should_have_empty_stock_of_given_item() {
      var coke = aColdBeverage("Coke", 1.5);
      var machineStock = new VendingMachineStock(Set.of(new ItemQuantity(coke, empty())));

      assertThat(machineStock.hasStock(coke)).isFalse();
    }

    @Test
    void should_not_have_stock_of_not_found_item_in_stock() {
      var coke = aColdBeverage("Coke", 1.5);
      var machineStock = new VendingMachineStock(Set.of());

      assertThatThrownBy(() -> machineStock.hasStock(coke))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  class FindStock {
    @Test
    void should_find_given_item_in_stock() {
      var coke = aColdBeverage("Coke", 1.5);
      var machineStock = new VendingMachineStock(Set.of(itemQuantity(coke, 5)));

      assertThat(machineStock.findStock(coke)).isPresent();
    }

    @Test
    void should_not_find_given_item_in_stock() {
      var coke = aColdBeverage("Coke", 1.5);
      var machineStock = new VendingMachineStock(Set.of());

      assertThat(machineStock.findStock(coke)).isEmpty();
    }
  }

  @Nested
  class QuantityInStock {
    @Test
    void should_return_quantity_in_stock_for_given_item() {
      var coke = aColdBeverage("Coke", 1.5);
      var machineStock = new VendingMachineStock(Set.of(itemQuantity(coke, 5)));

      assertThat(machineStock.quantityInStock(coke)).isEqualTo(new Quantity(5));
    }

    @Test
    void should_throw_exception_for_not_found_item_in_stock() {
      var coke = aColdBeverage("Coke", 1.5);
      var machineStock = new VendingMachineStock(Set.of());

      assertThatThrownBy(() -> machineStock.quantityInStock(coke))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  class AddStock {
    @Test
    void should_add_quantity_to_existing_item_stock() {
      var coke = aColdBeverage("Coke", 1.5);
      var machineStock =
          new VendingMachineStock(new HashMap<>(Map.of(coke.id(), itemQuantity(coke, 5))));

      var result = machineStock.addStock(itemQuantity(coke, 3));

      assertThat(result.findStock(coke)).contains(itemQuantity(coke, 8));
    }

    @Test
    void should_add_new_stock_if_item_not_exists() {

      var coke = aColdBeverage("Coke", 1.5);
      var machineStock = new VendingMachineStock(new HashMap<>());

      var result = machineStock.addStock(itemQuantity(coke, 3));

      assertThat(result.findStock(coke)).contains(itemQuantity(coke, 3));
    }
  }

  @Nested
  class DecrementStock {
    @Test
    void should_decrement_stock_of_given_item() {
      var coke = aColdBeverage("Coke", 1.5);
      var machineStock =
          new VendingMachineStock(new HashMap<>(Map.of(coke.id(), itemQuantity(coke, 5))));

      var result = machineStock.decrementStock(coke);

      assertThat(result.quantityInStock(coke)).isEqualTo(new Quantity(4));
    }

    @Test
    void should_throw_exception_when_item_not_found_in_stock() {
      var item =
          anItem().id(new ItemId(UUID.fromString("c29e78d0-e8fa-4c0e-82a9-0f05af4be3d2"))).build();
      var machineStock = new VendingMachineStock(Set.of());

      assertThatThrownBy(() -> machineStock.decrementStock(item))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("No stock available for item: " + item);
    }

    @Test
    void should_throw_exception_when_stock_is_empty() {
      var coke = aColdBeverage("Coke", 1.5);
      var machineStock =
          new VendingMachineStock(new HashMap<>(Map.of(coke.id(), new ItemQuantity(coke, empty()))));

      assertThatThrownBy(() -> machineStock.decrementStock(coke))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("No stock available for item: " + coke);
    }

    @Test
    void should_decrement_stock_to_zero_of_given_item() {
      var coke = aColdBeverage("Coke", 1.5);
      var machineStock =
          new VendingMachineStock(new HashMap<>(Map.of(coke.id(), itemQuantity(coke, 1))));

      var result = machineStock.decrementStock(coke);

      assertThat(result.quantityInStock(coke)).isEqualTo(empty());
    }
  }
}
