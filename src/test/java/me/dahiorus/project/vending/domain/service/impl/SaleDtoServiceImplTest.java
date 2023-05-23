package me.dahiorus.project.vending.domain.service.impl;

import static me.dahiorus.project.vending.domain.model.Sale.sell;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ItemMissing;
import me.dahiorus.project.vending.domain.exception.VendingMachineNotWorking;
import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.ItemType;
import me.dahiorus.project.vending.domain.model.PowerStatus;
import me.dahiorus.project.vending.domain.model.Stock;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.WorkingStatus;
import me.dahiorus.project.vending.domain.model.dto.ItemDTO;
import me.dahiorus.project.vending.domain.model.dto.SaleDTO;
import me.dahiorus.project.vending.domain.service.manager.SaleManager;
import me.dahiorus.project.vending.util.ItemBuilder;
import me.dahiorus.project.vending.util.VendingMachineBuilder;

@ExtendWith(MockitoExtension.class)
class SaleDtoServiceImplTest
{
  @Mock
  SaleManager manager;

  SaleDtoServiceImpl dtoService;

  @BeforeEach
  void setUp()
  {
    dtoService = new SaleDtoServiceImpl(manager, new DtoMapperImpl());
  }

  @Test
  void purchaseItem() throws Exception
  {
    VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.FOOD);
    Item item = buildItem("Chips", machine.getType(), BigDecimal.valueOf(1.5));
    addStock(machine, item, 10);

    when(manager.getWorkingMachine(machine.getId())).thenReturn(machine);
    when(manager.purchaseItem(machine, item)).thenReturn(sell(item, machine));

    ItemDTO itemToPurchase = new ItemDTO();
    itemToPurchase.setId(item.getId());
    itemToPurchase.setName(item.getName());
    itemToPurchase.setPrice(item.getPrice());

    SaleDTO sale = dtoService.purchaseItem(machine.getId(), itemToPurchase);

    assertThat(sale.getAmount()).isEqualTo(itemToPurchase.getPrice());
  }

  @Test
  void purchaseItemFromEmptyMachine() throws Exception
  {
    VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.FOOD);
    Item item = buildItem("Chips", machine.getType(), BigDecimal.valueOf(1.5));
    addStock(machine, item, 0);

    when(manager.getWorkingMachine(machine.getId())).thenReturn(machine);

    ItemDTO itemToPurchase = new ItemDTO();
    itemToPurchase.setId(item.getId());

    assertThatExceptionOfType(ItemMissing.class)
      .isThrownBy(() -> dtoService.purchaseItem(machine.getId(), itemToPurchase));
    verify(manager, never()).purchaseItem(machine, item);
  }

  @Test
  void purchaseUnknownItem() throws Exception
  {
    VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.FOOD);
    when(manager.getWorkingMachine(machine.getId())).thenReturn(machine);

    ItemDTO itemToPurchase = new ItemDTO();
    itemToPurchase.setId(UUID.randomUUID());

    assertThatExceptionOfType(ItemMissing.class)
      .isThrownBy(() -> dtoService.purchaseItem(machine.getId(), itemToPurchase));
    verify(manager, never()).purchaseItem(eq(machine), any());
  }

  @Test
  void purchaseFromUnknownMachine() throws Exception
  {
    UUID id = UUID.randomUUID();
    when(manager.getWorkingMachine(id)).thenThrow(new EntityNotFound(VendingMachine.class, id));

    ItemDTO itemToPurchase = new ItemDTO();
    itemToPurchase.setId(UUID.randomUUID());

    assertThatExceptionOfType(EntityNotFound.class)
      .isThrownBy(() -> dtoService.purchaseItem(id, itemToPurchase));
    verify(manager, never()).purchaseItem(any(), any());
  }

  @Test
  void purchaseFromNotWorkingMachine() throws Exception
  {
    VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.FOOD);
    machine.setPowerStatus(PowerStatus.OFF);
    when(manager.getWorkingMachine(machine.getId())).thenThrow(new VendingMachineNotWorking("Machine not working"));

    assertThatExceptionOfType(VendingMachineNotWorking.class)
      .isThrownBy(() -> dtoService.purchaseItem(machine.getId(), new ItemDTO()));
    verify(manager, never()).purchaseItem(any(), any());
  }

  static Item buildItem(final String name, final ItemType type, final BigDecimal price)
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
      .powerStatus(PowerStatus.ON)
      .workingStatus(WorkingStatus.OK)
      .build();
  }

  static void addStock(final VendingMachine machine, final Item item, final int quantity)
  {
    machine.getStocks()
      .clear();

    Stock stock = Stock.fill(item, quantity);
    stock.setId(UUID.randomUUID());
    machine.addStock(stock);
  }
}
