package me.dahiorus.project.vending.domain.machine.entity;

import static java.time.Month.JUNE;
import static me.dahiorus.project.vending.domain.item.entity.ItemType.COLD_BEVERAGE;
import static me.dahiorus.project.vending.domain.item.entity.ItemType.SNACK;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.defaultStatus;
import static me.dahiorus.project.vending.fixture.AddressFixture.anAddress;
import static me.dahiorus.project.vending.fixture.ItemFixture.aColdBeverage;
import static me.dahiorus.project.vending.fixture.ItemFixture.aHotBeverage;
import static me.dahiorus.project.vending.fixture.VendingMachineFixture.aVendingMachine;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class VendingMachineTest {
  @Test
  void should_have_last_intervention_marked_given_markIntervention() {
    var vendingMachine =
        new VendingMachine(
            new VendingMachineId(UUID.randomUUID()),
            SerialNumber.of("1234-5678"),
            anAddress().build(),
            COLD_BEVERAGE,
            defaultStatus(),
            null);
    assertThat(vendingMachine.lastIntervention()).isNull();

    var result = vendingMachine.markIntervention(LocalDateTime.of(2025, JUNE, 4, 11, 12, 0));

    assertThat(result.lastIntervention()).isEqualTo(LocalDateTime.of(2025, JUNE, 4, 11, 12, 0));
  }

  @Nested
  class Supports {
    @Test
    void should_return_true_when_item_type_matches() {
      var vendingMachine = aVendingMachine().itemType(COLD_BEVERAGE).build();
      var item = aColdBeverage("Coca-Cola", 1.50);

      assertThat(vendingMachine.supports(item)).isTrue();
    }

    @Test
    void should_return_false_when_item_type_does_not_match() {
      var vendingMachine = aVendingMachine().itemType(SNACK).build();
      var item = aHotBeverage("Hot tea", 1.50);

      assertThat(vendingMachine.supports(item)).isFalse();
    }
  }
}
