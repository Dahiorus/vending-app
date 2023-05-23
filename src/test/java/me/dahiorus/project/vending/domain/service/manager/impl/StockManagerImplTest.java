package me.dahiorus.project.vending.domain.service.manager.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.dahiorus.project.vending.domain.dao.DAO;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.PowerStatus;
import me.dahiorus.project.vending.domain.model.Stock;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.WorkingStatus;
import me.dahiorus.project.vending.util.ItemBuilder;
import me.dahiorus.project.vending.util.VendingMachineBuilder;

@ExtendWith(MockitoExtension.class)
class StockManagerImplTest
{
  @Mock
  DAO<Stock> dao;

  @Mock
  DAO<VendingMachine> vendingMachineDao;

  StockManagerImpl manager;

  @BeforeEach
  void setUp() throws Exception
  {
    manager = new StockManagerImpl(dao, vendingMachineDao);
  }

  @Nested
  class GetMachineTests
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

      VendingMachine workingMachine = manager.getMachine(id);

      assertThat(workingMachine).isEqualTo(machine);
    }

    @Test
    void getNonExistingMachine() throws Exception
    {
      UUID id = UUID.randomUUID();

      when(vendingMachineDao.read(id)).thenThrow(new EntityNotFound(VendingMachine.class, id));

      assertThatExceptionOfType(EntityNotFound.class).isThrownBy(() -> manager.getMachine(id));
    }
  }

  @Nested
  class ProvisionTests
  {
    VendingMachine machine;

    Item item;

    @BeforeEach
    void setUp()
    {
      machine = VendingMachineBuilder.builder()
        .id(UUID.randomUUID())
        .build();
      item = ItemBuilder.builder()
        .id(UUID.randomUUID())
        .name("item")
        .price(1.5)
        .build();

      assertThat(machine.getLastIntervention()).isNull();
    }

    @Test
    void provisionNewStock()
    {
      when(dao.save(any())).then(returnsFirstArg());
      
      manager.provision(machine, item, 10);
      
      assertAll(() -> assertThat(machine.getQuantityInStock(item)).isEqualTo(10),
        () -> assertThat(machine.getLastIntervention()).isNotNull());
      verify(vendingMachineDao).save(machine);
    }

    @Test
    void provisionExistingStock()
    {
      machine.addStock(Stock.fill(item, 5));
      when(dao.save(any())).then(returnsFirstArg());

      manager.provision(machine, item, 10);

      assertAll(() -> assertThat(machine.getQuantityInStock(item)).isEqualTo(15),
        () -> assertThat(machine.getLastIntervention()).isNotNull());
      verify(vendingMachineDao).save(machine);
    }
  }
}
