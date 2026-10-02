package me.dahiorus.project.vending.application.service.item;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import me.dahiorus.project.vending.domain.exception.ItemStillInStock;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.port.ItemRepositoryPort;
import me.dahiorus.project.vending.domain.stock.port.VendingMachineStockRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ItemApplicationServiceTest {

  @Mock ItemRepositoryPort itemRepository;
  @Mock VendingMachineStockRepositoryPort stockRepository;

  @InjectMocks ItemApplicationService itemApplicationService;

  @Test
  void should_delete_item_when_no_vending_machine_has_stock_of_it() {
    var id = new ItemId(randomUUID());
    given(stockRepository.isInStockOfAnyMachine(id)).willReturn(false);

    itemApplicationService.delete(id);

    then(itemRepository).should().delete(id);
  }

  @Test
  void should_not_delete_item_when_a_vending_machine_has_stock_of_it() {
    var id = new ItemId(randomUUID());
    given(stockRepository.isInStockOfAnyMachine(id)).willReturn(true);

    assertThatThrownBy(() -> itemApplicationService.delete(id))
        .isInstanceOf(ItemStillInStock.class);

    then(itemRepository).shouldHaveNoInteractions();
  }
}
