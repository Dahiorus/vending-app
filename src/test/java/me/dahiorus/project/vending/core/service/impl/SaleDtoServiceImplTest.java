package me.dahiorus.project.vending.core.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dahiorus.project.vending.util.ItemBuilder;
import com.dahiorus.project.vending.util.VendingMachineBuilder;

import me.dahiorus.project.vending.core.dao.impl.SaleDaoImpl;
import me.dahiorus.project.vending.core.dao.impl.StockDaoImpl;
import me.dahiorus.project.vending.core.dao.impl.VendingMachineDaoImpl;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ItemMissing;
import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.ItemType;
import me.dahiorus.project.vending.core.model.Stock;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.core.model.dto.SaleDTO;

@ExtendWith(MockitoExtension.class)
class SaleDtoServiceImplTest
{
  @Mock
  SaleDaoImpl dao;

  @Mock
  StockDaoImpl stockDao;

  @Mock
  VendingMachineDaoImpl vendingMachineDao;

  SaleDtoServiceImpl dtoService;

  @BeforeEach
  void setUp()
  {
    dtoService = new SaleDtoServiceImpl(dao, vendingMachineDao, stockDao, new DtoMapperImpl());
  }

  @Test
  void purchaseItem() throws Exception
  {
    VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.FOOD);
    Item item = buildItem("Chips", machine.getType(), 1.5);
    addStock(machine, item, 10);

    when(vendingMachineDao.read(machine.getId())).thenReturn(machine);
    when(vendingMachineDao.save(machine)).thenReturn(machine);
    when(dao.save(any())).then(invocation -> invocation.getArgument(0));

    ItemDTO itemToPurchase = new ItemDTO();
    itemToPurchase.setId(item.getId());
    itemToPurchase.setPrice(item.getPrice());

    SaleDTO sale = dtoService.purchaseItem(machine.getId(), itemToPurchase);

    assertAll(
        () -> assertThat(sale.getAmount()).isEqualTo(itemToPurchase.getPrice()),
        () -> assertThat(machine.getSales()).isNotEmpty());
  }

  @Test
  void purchaseItemFromEmptyMachine() throws Exception
  {
    VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.FOOD);
    Item item = buildItem("Chips", machine.getType(), 1.5);
    addStock(machine, item, 0);

    when(vendingMachineDao.read(machine.getId())).thenReturn(machine);

    ItemDTO itemToPurchase = new ItemDTO();
    itemToPurchase.setId(item.getId());

    assertThatExceptionOfType(ItemMissing.class)
      .isThrownBy(() -> dtoService.purchaseItem(machine.getId(), itemToPurchase));
    verify(vendingMachineDao, never()).save(machine);
    verify(dao, never()).save(any());
  }

  @Test
  void purchaseUnknownItem() throws Exception
  {
    VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.FOOD);
    when(vendingMachineDao.read(machine.getId())).thenReturn(machine);

    ItemDTO itemToPurchase = new ItemDTO();
    itemToPurchase.setId(UUID.randomUUID());

    assertThatExceptionOfType(ItemMissing.class)
      .isThrownBy(() -> dtoService.purchaseItem(machine.getId(), itemToPurchase));
    verify(vendingMachineDao, never()).save(machine);
    verify(dao, never()).save(any());
  }

  @Test
  void purchaseFromUnknownMachine() throws Exception
  {
    UUID id = UUID.randomUUID();
    when(vendingMachineDao.read(id)).thenThrow(new EntityNotFound(VendingMachine.class, id));

    ItemDTO itemToPurchase = new ItemDTO();
    itemToPurchase.setId(UUID.randomUUID());

    assertThatExceptionOfType(EntityNotFound.class)
      .isThrownBy(() -> dtoService.purchaseItem(id, itemToPurchase));
    verify(vendingMachineDao, never()).save(any());
    verify(dao, never()).save(any());
  }

  static Item buildItem(final String name, final ItemType type, final Double price)
  {
    return ItemBuilder.builder()
      .name(name)
      .type(type)
      .price(price)
      .build();
  }

  static VendingMachine buildMachine(final UUID id, final ItemType itemType)
  {
    return VendingMachineBuilder.builder()
      .id(id)
      .itemType(itemType)
      .build();
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
}
