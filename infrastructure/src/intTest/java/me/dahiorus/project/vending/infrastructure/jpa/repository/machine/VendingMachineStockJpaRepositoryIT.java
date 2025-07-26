package me.dahiorus.project.vending.infrastructure.jpa.repository.machine;

import static me.dahiorus.project.vending.domain.item.entity.ItemType.SNACK;
import static me.dahiorus.project.vending.fixture.ItemFixture.aColdBeverage;
import static me.dahiorus.project.vending.fixture.ItemFixture.aSnack;
import static me.dahiorus.project.vending.fixture.VendingMachineFixture.aVendingMachine;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.Set;
import java.util.UUID;
import me.dahiorus.project.vending.domain.item.entity.Item;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.stock.entity.ItemQuantity;
import me.dahiorus.project.vending.domain.stock.entity.Quantity;
import me.dahiorus.project.vending.domain.stock.entity.VendingMachineStock;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaItem;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaVendingMachine;
import me.dahiorus.project.vending.infrastructure.jpa.repository.H2DbContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = VendingMachineStockJpaRepositoryIT.TestConfig.class)
class VendingMachineStockJpaRepositoryIT extends H2DbContainer {

  @Autowired VendingMachineStockJpaRepository repository;

  VendingMachine vendingMachine;
  Item bueno, lays;

  @BeforeEach
  void setUpVendingMachineAndItems() {
    vendingMachine =
        aVendingMachine()
            .id(new VendingMachineId(UUID.fromString("c29e78d0-e8fa-4c0e-82a9-0f05af4be3d2")))
            .itemType(SNACK)
            .build();
    entityManager.persist(JpaVendingMachine.fromDomain(vendingMachine));

    bueno = aColdBeverage("Kinder Bueno", 1.8);
    lays = aSnack("Lays", 1.5);
    entityManager.persist(JpaItem.fromDomain(bueno));
    entityManager.persist(JpaItem.fromDomain(lays));

    entityManager.flush();
  }

  @Nested
  class Update {
    @Test
    void should_add_stock_to_empty_vending_machine_stock() {
      var stockToAdd =
          new VendingMachineStock(
              Set.of(
                  new ItemQuantity(bueno, new Quantity(5)),
                  new ItemQuantity(lays, new Quantity(2))));

      var result = repository.update(vendingMachine.id(), stockToAdd);

      assertThat(result)
          .isEqualTo(
              new VendingMachineStock(
                  Set.of(
                      new ItemQuantity(bueno, new Quantity(5)),
                      new ItemQuantity(lays, new Quantity(2)))));
    }

    @Test
    void should_update_existing_stock_in_vending_machine() {
      // Given
      var initialStock =
          new VendingMachineStock(
              Set.of(
                  new ItemQuantity(bueno, new Quantity(5)),
                  new ItemQuantity(lays, new Quantity(2))));
      repository.update(vendingMachine.id(), initialStock);
      entityManager.flush();

      var stockToUpdate = new VendingMachineStock(Set.of(new ItemQuantity(lays, new Quantity(4))));

      // When
      var result = repository.update(vendingMachine.id(), stockToUpdate);

      // Then
      assertThat(result)
          .isEqualTo(
              new VendingMachineStock(
                  Set.of(
                      new ItemQuantity(bueno, new Quantity(5)),
                      new ItemQuantity(lays, new Quantity(4)))));
    }

    @Test
    void should_remove_empty_stock_from_vending_machine() {
      // Given
      var initialStock =
          new VendingMachineStock(
              Set.of(
                  new ItemQuantity(bueno, new Quantity(5)),
                  new ItemQuantity(lays, new Quantity(2))));
      repository.update(vendingMachine.id(), initialStock);
      entityManager.flush();

      var stockToRemove = new VendingMachineStock(Set.of(new ItemQuantity(lays, new Quantity(0))));

      // When
      var result = repository.update(vendingMachine.id(), stockToRemove);

      // Then
      assertThat(result)
          .isEqualTo(new VendingMachineStock(Set.of(new ItemQuantity(bueno, new Quantity(5)))));
    }
  }

  @Nested
  class Find {
    @Test
    void should_return_empty_when_find_non_existing_vending_machine_stock() {
      var result =
          repository.find(
              new VendingMachineId(UUID.fromString("c31e78d0-e8fa-6c0e-82a9-0f05af4be3d2")));

      assertThat(result).isEmpty();
    }

    @Test
    void should_return_stock_of_given_vending_machine() {
      var result = repository.find(vendingMachine.id());

      assertThat(result).contains(new VendingMachineStock(Set.of()));
    }
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    VendingMachineStockJpaRepository repository(EntityManager entityManager) {
      return new VendingMachineStockJpaRepository(entityManager);
    }
  }
}
