package me.dahiorus.project.vending.domain.machine.usecase;

import static java.time.LocalDateTime.now;
import static me.dahiorus.project.vending.domain.item.entity.ItemType.SNACK;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.WorkingStatus.ERROR;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.WorkingStatus.WORKING;
import static me.dahiorus.project.vending.domain.stock.entity.Quantity.empty;
import static me.dahiorus.project.vending.fixture.ItemFixture.aSnack;
import static me.dahiorus.project.vending.fixture.VendingMachineFixture.aVendingMachineWithWorkingStatus;
import static me.dahiorus.project.vending.fixture.VendingMachineStocksFixture.emptyStock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.Optional;
import java.util.UUID;
import me.dahiorus.project.vending.domain.exception.ItemStockIsEmpty;
import me.dahiorus.project.vending.domain.exception.NotWorkingVendingMachine;
import me.dahiorus.project.vending.domain.item.port.ItemRepositoryPort;
import me.dahiorus.project.vending.domain.machine.entity.ClientOrder;
import me.dahiorus.project.vending.domain.machine.entity.ClientOrder.OrderedItem;
import me.dahiorus.project.vending.domain.machine.entity.ClientOrderId;
import me.dahiorus.project.vending.domain.machine.port.ClientOrderRepositoryPort;
import me.dahiorus.project.vending.domain.machine.port.VendingMachineRepositoryPort;
import me.dahiorus.project.vending.domain.stock.entity.ItemQuantity;
import me.dahiorus.project.vending.domain.stock.entity.Quantity;
import me.dahiorus.project.vending.domain.stock.entity.VendingMachineStock;
import me.dahiorus.project.vending.domain.stock.port.VendingMachineStockRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderItemTest {

  @Mock VendingMachineRepositoryPort vendingMachineRepository;
  @Mock ItemRepositoryPort itemRepository;
  @Mock VendingMachineStockRepositoryPort vendingMachineStockRepository;
  @Mock ClientOrderRepositoryPort clientOrderRepository;
  @InjectMocks OrderItem orderItem;

  @Captor ArgumentCaptor<VendingMachineStock> vendingMachineStockCaptor;

  @Test
  void should_order_given_item_on_given_working_vending_machine() {
    var vendingMachine = aVendingMachineWithWorkingStatus(WORKING).itemType(SNACK).build();
    var kinderBueno = aSnack("Kinder Bueno", 2.0);
    var stock = emptyStock().addStock(new ItemQuantity(kinderBueno, new Quantity(10)));

    given(vendingMachineRepository.find(vendingMachine.id()))
        .willReturn(Optional.of(vendingMachine));
    given(itemRepository.find(kinderBueno.id())).willReturn(Optional.of(kinderBueno));
    given(vendingMachineStockRepository.find(vendingMachine.id())).willReturn(Optional.of(stock));

    var createdOrder =
        new ClientOrder(
            new ClientOrderId(UUID.randomUUID()),
            vendingMachine,
            new OrderedItem(kinderBueno.id(), kinderBueno.name(), kinderBueno.price()),
            now());
    given(clientOrderRepository.create(vendingMachine.id(), kinderBueno.id()))
        .willReturn(createdOrder);

    var resultOrder = orderItem.execute(vendingMachine.id(), kinderBueno.id());

    then(vendingMachineStockRepository)
        .should()
        .update(eq(vendingMachine.id()), vendingMachineStockCaptor.capture());
    assertThat(vendingMachineStockCaptor.getValue().findStock(kinderBueno))
        .map(ItemQuantity::quantity)
        .contains(new Quantity(9));
    assertThat(resultOrder).isEqualTo(createdOrder);
  }

  @Test
  void should_throw_exception_when_vending_machine_not_working() {
    var vendingMachine = aVendingMachineWithWorkingStatus(ERROR).itemType(SNACK).build();
    var kinderBueno = aSnack("Kinder Bueno", 2.0);

    given(vendingMachineRepository.find(vendingMachine.id()))
        .willReturn(Optional.of(vendingMachine));

    assertThatThrownBy(() -> orderItem.execute(vendingMachine.id(), kinderBueno.id()))
        .isInstanceOf(NotWorkingVendingMachine.class);
    then(vendingMachineStockRepository).shouldHaveNoInteractions();
    then(clientOrderRepository).shouldHaveNoInteractions();
  }

  @Test
  void should_throw_exception_when_given_item_stock_is_empty() {
    var vendingMachine = aVendingMachineWithWorkingStatus(WORKING).itemType(SNACK).build();
    var kinderBueno = aSnack("Kinder Bueno", 2.0);
    var stock = emptyStock().addStock(new ItemQuantity(kinderBueno, empty()));

    given(vendingMachineRepository.find(vendingMachine.id()))
        .willReturn(Optional.of(vendingMachine));
    given(itemRepository.find(kinderBueno.id())).willReturn(Optional.of(kinderBueno));
    given(vendingMachineStockRepository.find(vendingMachine.id())).willReturn(Optional.of(stock));

    assertThatThrownBy(() -> orderItem.execute(vendingMachine.id(), kinderBueno.id()))
        .isInstanceOf(ItemStockIsEmpty.class);
    then(vendingMachineStockRepository).should(never()).update(eq(vendingMachine.id()), any());
    then(clientOrderRepository).shouldHaveNoInteractions();
  }

  @Test
  void should_throw_exception_when_given_item_not_in_vending_machine_stock() {
    var vendingMachine = aVendingMachineWithWorkingStatus(WORKING).build();
    var kinderBueno = aSnack("Kinder Bueno", 2.0);
    var stock = emptyStock();

    given(vendingMachineRepository.find(vendingMachine.id()))
        .willReturn(Optional.of(vendingMachine));
    given(itemRepository.find(kinderBueno.id())).willReturn(Optional.of(kinderBueno));
    given(vendingMachineStockRepository.find(vendingMachine.id())).willReturn(Optional.of(stock));

    assertThatThrownBy(() -> orderItem.execute(vendingMachine.id(), kinderBueno.id()))
        .isInstanceOf(IllegalArgumentException.class);
    then(vendingMachineStockRepository).should(never()).update(eq(vendingMachine.id()), any());
    then(clientOrderRepository).shouldHaveNoInteractions();
  }
}
