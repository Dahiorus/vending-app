package me.dahiorus.project.vending.domain.stock.usecase;

import static java.time.Instant.now;
import static java.time.ZoneId.systemDefault;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static me.dahiorus.project.vending.domain.item.entity.ItemType.COLD_BEVERAGE;
import static me.dahiorus.project.vending.domain.item.entity.ItemType.SNACK;
import static me.dahiorus.project.vending.fixture.ItemFixture.aSnack;
import static me.dahiorus.project.vending.fixture.VendingMachineFixture.aVendingMachine;
import static me.dahiorus.project.vending.fixture.VendingMachineStocksFixture.emptyStock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.exception.UnsupportedItemToProvision;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.port.ItemRepositoryPort;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineToUpdate;
import me.dahiorus.project.vending.domain.machine.port.VendingMachineRepositoryPort;
import me.dahiorus.project.vending.domain.stock.entity.ItemQuantity;
import me.dahiorus.project.vending.domain.stock.entity.Quantity;
import me.dahiorus.project.vending.domain.stock.entity.VendingMachineStock;
import me.dahiorus.project.vending.domain.stock.port.VendingMachineStockRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProvisionVendingMachineStockTest {
  @Mock(strictness = LENIENT)
  Clock clock;

  @Mock VendingMachineRepositoryPort vendingMachineRepository;
  @Mock ItemRepositoryPort itemRepository;
  @Mock VendingMachineStockRepositoryPort vendingMachineStockRepository;
  @InjectMocks ProvisionVendingMachineStock provisionVendingMachineStock;

  @Captor ArgumentCaptor<VendingMachineStock> vendingMachineStockCaptor;

  @BeforeEach
  void setUpClock() {
    var fixedClock = Clock.fixed(now(), systemDefault());
    doReturn(fixedClock.instant()).when(clock).instant();
    doReturn(fixedClock.getZone()).when(clock).getZone();
  }

  @Test
  void should_provision_vending_machine_with_item_stock() {
    // Given
    var machine = aVendingMachine().itemType(SNACK).build();
    var curly = aSnack("Curly 60g", 2.0);
    var quantity = new Quantity(10);

    given(vendingMachineRepository.find(machine.id())).willReturn(of(machine));
    given(itemRepository.find(curly.id())).willReturn(of(curly));
    given(vendingMachineStockRepository.find(machine.id())).willReturn(of(emptyStock()));

    // When
    var result = provisionVendingMachineStock.execute(machine.id(), curly.id(), quantity);

    // Then
    then(vendingMachineStockRepository)
        .should()
        .update(eq(machine.id()), vendingMachineStockCaptor.capture());
    then(vendingMachineRepository)
        .should()
        .update(
            new VendingMachineToUpdate(
                machine.id(), machine.address(), machine.status(), LocalDateTime.now(clock)));
    assertThat(result.findStock(curly)).contains(new ItemQuantity(curly, quantity));
  }

  @Test
  void should_throw_exception_given_unsupported_item() {
    // Given
    var machine = aVendingMachine().itemType(COLD_BEVERAGE).build();
    var coke = aSnack("Coke 330ml", 1.5);
    var quantity = new Quantity(10);

    given(vendingMachineRepository.find(machine.id())).willReturn(of(machine));
    given(itemRepository.find(coke.id())).willReturn(of(coke));

    // When / Then
    assertThatThrownBy(
            () -> provisionVendingMachineStock.execute(machine.id(), coke.id(), quantity))
        .isInstanceOf(UnsupportedItemToProvision.class)
        .hasMessage(
            "Cannot provision unsupported item 'Coke 330ml' in vending machine 'VM-123456'.");
    then(vendingMachineStockRepository).shouldHaveNoInteractions();
  }

  @Test
  void should_throw_exception_when_item_not_found() {
    // Given
    var machine = aVendingMachine().itemType(SNACK).build();
    var itemId = new ItemId(UUID.fromString("12345678-1234-1234-1234-123456789012"));
    var quantity = new Quantity(10);

    given(vendingMachineRepository.find(machine.id())).willReturn(of(machine));
    given(itemRepository.find(itemId)).willReturn(empty());

    // When / Then
    assertThatThrownBy(() -> provisionVendingMachineStock.execute(machine.id(), itemId, quantity))
        .isInstanceOf(ResourceNotFound.class)
        .hasMessage(
            "Resource not found with ID: ItemId[value=12345678-1234-1234-1234-123456789012]");
    then(vendingMachineStockRepository).shouldHaveNoInteractions();
  }

  @Test
  void should_throw_exception_when_machine_not_found() {
    // Given
    var machineId = new VendingMachineId(UUID.fromString("12345678-1234-1234-1234-123456789012"));
    var itemId = new ItemId(UUID.fromString("12345678-1234-1234-1234-123456789012"));
    var quantity = new Quantity(10);

    given(vendingMachineRepository.find(machineId)).willReturn(empty());

    // When / Then
    assertThatThrownBy(() -> provisionVendingMachineStock.execute(machineId, itemId, quantity))
        .isInstanceOf(ResourceNotFound.class)
        .hasMessage(
            "Resource not found with ID: VendingMachineId[value=12345678-1234-1234-1234-123456789012]");
    then(itemRepository).shouldHaveNoInteractions();
    then(vendingMachineStockRepository).shouldHaveNoInteractions();
  }

  @Test
  void should_throw_exception_when_machine_stock_not_found() {
    // Given
    var machine = aVendingMachine().itemType(SNACK).build();
    var curly = aSnack("Curly 60g", 2.0);
    var quantity = new Quantity(10);

    given(vendingMachineRepository.find(machine.id())).willReturn(of(machine));
    given(itemRepository.find(curly.id())).willReturn(of(curly));
    given(vendingMachineStockRepository.find(machine.id())).willReturn(empty());

    // When / Then
    assertThatThrownBy(
            () -> provisionVendingMachineStock.execute(machine.id(), curly.id(), quantity))
        .isInstanceOf(ResourceNotFound.class)
        .hasMessage(
            "Resource not found with ID: VendingMachineId[value=" + machine.id().value() + "]");
    then(vendingMachineStockRepository).should(never()).update(eq(machine.id()), any());
    then(vendingMachineRepository).shouldHaveNoMoreInteractions();
  }
}
