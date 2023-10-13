package me.dahiorus.project.vending.domain.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.ItemBuilder;
import me.dahiorus.project.vending.domain.model.ItemType;
import me.dahiorus.project.vending.domain.model.Stock;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.VendingMachineBuilder;
import me.dahiorus.project.vending.domain.model.dto.ItemDto;
import me.dahiorus.project.vending.domain.model.dto.StockDto;
import me.dahiorus.project.vending.domain.model.dto.StockQuantityDto;
import me.dahiorus.project.vending.domain.service.manager.StockManager;
import me.dahiorus.project.vending.domain.service.validation.impl.StockValidatorImpl;

@ExtendWith(MockitoExtension.class)
class StockDtoServiceImplTest
{
  @Mock
  StockManager manager;

  StockDtoServiceImpl dtoService;

  @BeforeEach
  void setUp()
  {
    dtoService = new StockDtoServiceImpl(manager, new StockValidatorImpl(),
      new DtoMapperImpl());
  }

  @Nested
  class ProvisionTests
  {
    @Test
    void provisionValidItem() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(),
        ItemType.COLD_BAVERAGE);
      Item item = buildItem(UUID.randomUUID(), "CocaCola",
        ItemType.COLD_BAVERAGE);
      addStock(machine, item, 5);

      when(manager.getMachine(machine.getId())).thenReturn(machine);

      ItemDto itemDto = new ItemDto();
      itemDto.setId(item.getId());
      itemDto.setName(item.getName());
      itemDto.setType(machine.getType());

      assertThatNoException().isThrownBy(
        () -> dtoService.provisionStock(machine.getId(), new StockQuantityDto(itemDto, 10)));
      verify(manager).provision(machine, item, 10);
    }

    @Test
    void cannotProvisionOtherItemType() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(),
        ItemType.COLD_BAVERAGE);
      ItemDto itemDto = new ItemDto();
      itemDto.setName("Lays");
      itemDto.setType(ItemType.FOOD);

      when(manager.getMachine(machine.getId())).thenReturn(machine);

      assertThatExceptionOfType(ValidationException.class)
        .isThrownBy(
          () -> dtoService.provisionStock(machine.getId(), new StockQuantityDto(itemDto, 10)));
      verify(manager, never()).provision(eq(machine), any(), eq(10));
    }

    @Test
    void cannotProvisionUnknownMachine() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(manager.getMachine(id))
        .thenThrow(new EntityNotFound(VendingMachine.class, id));

      assertThatExceptionOfType(EntityNotFound.class)
        .isThrownBy(() -> dtoService.provisionStock(id, new StockQuantityDto(new ItemDto(), 15)));
      verify(manager, never()).provision(any(), any(), eq(15));
    }

    @ParameterizedTest(name = "Cannot provision {0} quantity of item")
    @ValueSource(ints = { 0, -50 })
    void canOnlyProvisiongPositiveQuantity(final Integer quantity)
      throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(),
        ItemType.COLD_BAVERAGE);
      when(manager.getMachine(machine.getId())).thenReturn(machine);

      ItemDto itemDto = new ItemDto();
      itemDto.setName("CocaCola");
      itemDto.setType(machine.getType());

      assertThatExceptionOfType(ValidationException.class)
        .isThrownBy(
          () -> dtoService.provisionStock(machine.getId(), new StockQuantityDto(itemDto, quantity)));

      verify(manager, never()).provision(any(), any(), eq(quantity));
    }
  }

  @Nested
  class GetStocksTests
  {
    @Test
    void getStocks() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(),
        ItemType.COLD_BAVERAGE);
      addStock(machine, buildItem(null, "Item1", machine.getType()), 12);
      addStock(machine, buildItem(null, "Item2", machine.getType()), 0);

      when(manager.getMachine(machine.getId())).thenReturn(machine);

      List<StockDto> stocks = dtoService.getStocks(machine.getId());

      assertThat(stocks).hasSize(2)
        .anySatisfy(
          s -> assertThat(s).hasFieldOrPropertyWithValue("itemName", "Item1")
            .hasFieldOrPropertyWithValue("quantity", 12))
        .anySatisfy(
          s -> assertThat(s).hasFieldOrPropertyWithValue("itemName", "Item2")
            .hasFieldOrPropertyWithValue("quantity", 0));
    }

    @Test
    void getStocksOfNonExistingMachine() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(manager.getMachine(id))
        .thenThrow(new EntityNotFound(VendingMachine.class, id));

      assertThatExceptionOfType(EntityNotFound.class)
        .isThrownBy(() -> dtoService.getStocks(id));
    }
  }

  static void addStock(final VendingMachine machine, final Item item,
    final int quantity)
  {
    Stock stock = Stock.fill(item, quantity);
    stock.setId(UUID.randomUUID());
    machine.addStock(stock);
  }

  static VendingMachine buildMachine(final UUID randomUUID, final ItemType type)
  {
    return VendingMachineBuilder.builder()
      .id(randomUUID)
      .itemType(type)
      .build();
  }

  static Item buildItem(final UUID id, final String name, final ItemType type)
  {
    return ItemBuilder.builder()
      .id(id)
      .name(name)
      .type(type)
      .build();
  }
}
