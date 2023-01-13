package me.dahiorus.project.vending.domain.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
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
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.dahiorus.project.vending.domain.dao.StockDAO;
import me.dahiorus.project.vending.domain.dao.VendingMachineDAO;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.ItemType;
import me.dahiorus.project.vending.domain.model.Stock;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.dto.ItemDTO;
import me.dahiorus.project.vending.domain.model.dto.StockDTO;
import me.dahiorus.project.vending.domain.service.validation.impl.StockValidatorImpl;
import me.dahiorus.project.vending.util.ItemBuilder;
import me.dahiorus.project.vending.util.VendingMachineBuilder;

@ExtendWith(MockitoExtension.class)
class StockDtoServiceImplTest
{
  @Mock
  StockDAO dao;

  @Mock
  VendingMachineDAO vendingMachineDao;

  StockDtoServiceImpl dtoService;

  @BeforeEach
  void setUp()
  {
    dtoService = new StockDtoServiceImpl(dao, vendingMachineDao, new StockValidatorImpl(), new DtoMapperImpl());
  }

  @Nested
  class ProvisionTests
  {
    @Test
    void provisionNewItem() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.COLD_BAVERAGE);
      ItemDTO itemDto = new ItemDTO();
      itemDto.setName("CocaCola");
      itemDto.setType(machine.getType());

      when(vendingMachineDao.read(machine.getId())).thenReturn(machine);
      when(vendingMachineDao.save(machine)).thenReturn(machine);

      assertThatNoException().isThrownBy(() -> dtoService.provisionStock(machine.getId(), itemDto, 10));

      Stock stock = machine.getStocks()
        .get(0);
      assertAll(() -> assertThat(machine.getLastIntervention()).isNotNull(),
        () -> assertThat(stock.getQuantity()).isEqualTo(10),
        () -> assertThat(stock).extracting(Stock::getItem)
          .hasFieldOrPropertyWithValue("name", itemDto.getName()));
      verify(dao).save(stock);
    }

    @Test
    void provisionExistingItem() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.COLD_BAVERAGE);
      Item item = buildItem(UUID.randomUUID(), "CocaCola", ItemType.COLD_BAVERAGE);
      addStock(machine, item, 5);

      when(vendingMachineDao.read(machine.getId())).thenReturn(machine);
      when(vendingMachineDao.save(machine)).thenReturn(machine);

      ItemDTO itemDto = new ItemDTO();
      itemDto.setId(item.getId());
      itemDto.setName(item.getName());
      itemDto.setType(machine.getType());

      assertThatNoException().isThrownBy(() -> dtoService.provisionStock(machine.getId(), itemDto, 10));
      assertAll(() -> assertThat(machine.getLastIntervention()).isNotNull(),
        () -> assertThat(machine.getQuantityInStock(item)).isEqualTo(15L));
      verify(dao).save(any());
    }

    @Test
    void cannotProvisionOtherItemType() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.COLD_BAVERAGE);
      ItemDTO itemDto = new ItemDTO();
      itemDto.setName("Lays");
      itemDto.setType(ItemType.FOOD);

      when(vendingMachineDao.read(machine.getId())).thenReturn(machine);

      assertThatExceptionOfType(ValidationException.class)
        .isThrownBy(() -> dtoService.provisionStock(machine.getId(), itemDto, 10));
      verify(vendingMachineDao, never()).save(machine);
      verify(dao, never()).save(any());
    }

    @Test
    void cannotProvisionUnknownMachine() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(vendingMachineDao.read(id)).thenThrow(new EntityNotFound(VendingMachine.class, id));

      assertThatExceptionOfType(EntityNotFound.class)
        .isThrownBy(() -> dtoService.provisionStock(id, new ItemDTO(), 15));
      verify(vendingMachineDao, never()).save(any());
      verify(dao, never()).save(any());
    }

    @ParameterizedTest(name = "Cannot provision {0} quantity of item")
    @NullSource
    @ValueSource(ints = { 0, -50 })
    void canOnlyProvisiongPositiveQuantity(final Integer quantity) throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.COLD_BAVERAGE);
      when(vendingMachineDao.read(machine.getId())).thenReturn(machine);

      ItemDTO itemDto = new ItemDTO();
      itemDto.setName("CocaCola");
      itemDto.setType(machine.getType());

      assertThatExceptionOfType(ValidationException.class)
        .isThrownBy(() -> dtoService.provisionStock(machine.getId(), itemDto, quantity));

      verify(dao, never()).save(any());
    }
  }

  @Nested
  class GetStocksTests
  {
    @Test
    void getStocks() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.COLD_BAVERAGE);
      addStock(machine, buildItem(null, "Item1", machine.getType()), 12);
      addStock(machine, buildItem(null, "Item2", machine.getType()), 0);

      when(vendingMachineDao.read(machine.getId())).thenReturn(machine);

      List<StockDTO> stocks = dtoService.getStocks(machine.getId());

      assertThat(stocks).hasSize(2)
        .anySatisfy(s -> assertThat(s).hasFieldOrPropertyWithValue("itemName", "Item1")
          .hasFieldOrPropertyWithValue("quantity", 12))
        .anySatisfy(s -> assertThat(s).hasFieldOrPropertyWithValue("itemName", "Item2")
          .hasFieldOrPropertyWithValue("quantity", 0));
    }

    @Test
    void getStocksOfNonExistingMachine() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(vendingMachineDao.read(id)).thenThrow(new EntityNotFound(VendingMachine.class, id));

      assertThatExceptionOfType(EntityNotFound.class).isThrownBy(() -> dtoService.getStocks(id));
    }
  }

  static void addStock(final VendingMachine machine, final Item item, final int quantity)
  {
    Stock stock = new Stock();
    stock.setId(UUID.randomUUID());
    stock.setItem(item);
    stock.setQuantity(quantity);
    machine.getStocks()
      .add(stock);
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
