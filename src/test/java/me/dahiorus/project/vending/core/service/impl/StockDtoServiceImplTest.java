package me.dahiorus.project.vending.core.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dahiorus.project.vending.util.VendingMachineBuilder;

import me.dahiorus.project.vending.core.dao.impl.StockDaoImpl;
import me.dahiorus.project.vending.core.dao.impl.VendingMachineDaoImpl;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.ItemType;
import me.dahiorus.project.vending.core.model.Stock;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.core.service.validation.impl.StockValidatorImpl;

@ExtendWith(MockitoExtension.class)
class StockDtoServiceImplTest
{
  @Mock
  StockDaoImpl dao;

  StockValidatorImpl stockValidator;

  @Mock
  VendingMachineDaoImpl vendingMachineDao;

  StockDtoServiceImpl dtoService;

  @BeforeEach
  void setUp()
  {
    stockValidator = new StockValidatorImpl();
    dtoService = new StockDtoServiceImpl(dao, vendingMachineDao, stockValidator, new DtoMapperImpl());
  }

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
    Item item = new Item();
    item.setId(UUID.randomUUID());
    item.setName("CocaCola");
    addStock(machine, item, 5);

    when(vendingMachineDao.read(machine.getId())).thenReturn(machine);
    when(vendingMachineDao.save(machine)).thenReturn(machine);

    ItemDTO itemDto = new ItemDTO();
    itemDto.setId(item.getId());
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

  static void addStock(final VendingMachine machine, final Item item, final int quantity)
  {
    machine.getStocks()
      .clear();

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
}
