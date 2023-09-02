package me.dahiorus.project.vending.domain.service.manager.impl;

import static me.dahiorus.project.vending.domain.model.Stock.fill;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.dahiorus.project.vending.domain.dao.Dao;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.VendingMachineNotWorking;
import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.PowerStatus;
import me.dahiorus.project.vending.domain.model.Sale;
import me.dahiorus.project.vending.domain.model.Stock;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.WorkingStatus;
import me.dahiorus.project.vending.util.ItemBuilder;
import me.dahiorus.project.vending.util.VendingMachineBuilder;

@ExtendWith(MockitoExtension.class)
class SaleManagerImplTest
{
  @Mock
  Dao<Sale> dao;

  @Mock
  Dao<VendingMachine> vendingMachineDao;

  @Mock
  Dao<Stock> stockDao;

  SaleManagerImpl manager;

  @BeforeEach
  void setUp() throws Exception
  {
    manager = new SaleManagerImpl(dao, vendingMachineDao, stockDao);
  }

  @Test
  void purchaseItem()
  {
    VendingMachine machine = VendingMachineBuilder.builder()
      .id(UUID.randomUUID())
      .build();
    Item item = ItemBuilder.builder()
      .id(UUID.randomUUID())
      .name("item")
      .price(BigDecimal.valueOf(1.5))
      .build();
    machine.addStock(fill(item, 5));

    when(stockDao.save(any())).then(returnsFirstArg());
    when(dao.save(any())).then(returnsFirstArg());

    Sale sale = manager.purchaseItem(machine, item);

    assertAll(() -> assertThat(sale).hasFieldOrPropertyWithValue("amount", BigDecimal.valueOf(1.5))
      .hasFieldOrPropertyWithValue("machine", machine),
      () -> assertThat(machine.getQuantityInStock(item)).isEqualTo(4));
    verify(vendingMachineDao).save(machine);
  }

  @Nested
  class GetWorkingMachineTests
  {
    @Test
    void getWorkingMachine() throws Exception
    {
      UUID id = UUID.randomUUID();
      VendingMachine machine = VendingMachineBuilder.builder()
        .id(id)
        .powerStatus(PowerStatus.ON)
        .workingStatus(WorkingStatus.OK)
        .build();
      when(vendingMachineDao.read(id)).thenReturn(machine);

      VendingMachine workingMachine = manager.getWorkingMachine(id);

      assertThat(workingMachine).isEqualTo(machine);
    }

    @Test
    void getMachineNotWorkingThrowsException() throws Exception
    {
      UUID id = UUID.randomUUID();
      VendingMachine machine = VendingMachineBuilder.builder()
        .id(id)
        .powerStatus(PowerStatus.ON)
        .workingStatus(WorkingStatus.ERROR)
        .build();
      when(vendingMachineDao.read(id)).thenReturn(machine);

      assertThatExceptionOfType(VendingMachineNotWorking.class)
        .isThrownBy(() -> manager.getWorkingMachine(id));
    }

    @Test
    void getNonExistingMachine() throws Exception
    {
      UUID id = UUID.randomUUID();

      when(vendingMachineDao.read(id)).thenThrow(new EntityNotFound(VendingMachine.class, id));

      assertThatExceptionOfType(EntityNotFound.class)
        .isThrownBy(() -> manager.getWorkingMachine(id));
    }
  }
}
