package me.dahiorus.project.vending.core.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.dahiorus.project.vending.core.dao.ReportDAO;
import me.dahiorus.project.vending.core.dao.VendingMachineDAO;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.model.ChangeSystemStatus;
import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.ItemType;
import me.dahiorus.project.vending.core.model.PowerStatus;
import me.dahiorus.project.vending.core.model.Report;
import me.dahiorus.project.vending.core.model.Sale;
import me.dahiorus.project.vending.core.model.Stock;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.model.WorkingStatus;
import me.dahiorus.project.vending.core.model.dto.ReportDTO;

@ExtendWith(MockitoExtension.class)
class ReportDtoServiceImplTest
{
  @Mock(lenient = true)
  ReportDAO dao;

  @Mock
  VendingMachineDAO vendingMachineDao;

  ReportDtoServiceImpl controller;

  @Captor
  ArgumentCaptor<Report> reportArg;

  @BeforeEach
  void setUp()
  {
    when(dao.getDomainClass()).thenReturn(Report.class);
    controller = new ReportDtoServiceImpl(dao, new DtoMapperImpl(), vendingMachineDao);

    when(dao.save(any()))
      .then(invocation -> {
        Report arg = invocation.getArgument(0);
        arg.setId(UUID.randomUUID());
        arg.setCreatedAt(Instant.now());
        return arg;
      });
  }

  @Test
  void createIsUnsupported()
  {
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> controller.create(new ReportDTO()));
  }

  @Test
  void updateIsUnsupported()
  {
    assertThatExceptionOfType(UnsupportedOperationException.class)
      .isThrownBy(() -> controller.update(UUID.randomUUID(), new ReportDTO()));
  }

  @Nested
  class ReportTests
  {
    @Test
    void reportUnkownMachine() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(vendingMachineDao.read(id)).thenThrow(new EntityNotFound(VendingMachine.class, id));

      assertThatExceptionOfType(EntityNotFound.class).isThrownBy(() -> controller.report(id));
      verify(dao, never()).save(any());
    }

    @Test
    void reportVendingMachine() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID());
      when(vendingMachineDao.read(machine.getId())).thenReturn(machine);

      ReportDTO report = controller.report(machine.getId());

      assertReportHasMachineInfo(report, machine);
    }

    @Test
    void reportMachineWithStock() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID());
      Item item = new Item();
      item.setId(UUID.randomUUID());
      item.setName("Mars");
      item.setPrice(1.2);
      item.setType(machine.getType());
      Stock stock = new Stock();
      stock.setId(UUID.randomUUID());
      stock.setItem(item);
      stock.setQuantity(12);
      machine.setStocks(List.of(stock));

      when(vendingMachineDao.read(machine.getId())).thenReturn(machine);

      ReportDTO report = controller.report(machine.getId());

      assertAll(
          () -> assertReportHasMachineInfo(report, machine),
          () -> assertThat(report.getReportStocks()
            .get(0)).hasFieldOrPropertyWithValue("itemName", item.getName())
              .hasFieldOrPropertyWithValue("quantity", stock.getQuantity()));
    }

    @Test
    void reportVendingMachineWithSales() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID());
      Sale sale1 = new Sale();
      sale1.setAmount(1.2);
      sale1.setMachine(machine);
      Sale sale2 = new Sale();
      sale2.setAmount(1.5);
      sale2.setMachine(machine);
      machine.setSales(List.of(sale1, sale2));

      when(vendingMachineDao.read(machine.getId())).thenReturn(machine);

      ReportDTO report = controller.report(machine.getId());

      assertAll(() -> assertReportHasMachineInfo(report, machine),
          () -> assertThat(report.getTotalSaleAmount()).isEqualTo(sale1.getAmount() + sale2.getAmount()));
    }

    @Test
    void reportVendingMachineHavingOldReport() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID());
      Sale sale1 = new Sale();
      sale1.setAmount(1.2);
      sale1.setMachine(machine);
      sale1.setCreatedAt(Instant.parse("2022-04-01T10:15:30Z"));
      Sale sale2 = new Sale();
      sale2.setAmount(1.5);
      sale2.setMachine(machine);
      sale2.setCreatedAt(Instant.parse("2022-04-10T10:15:30Z"));
      machine.setSales(List.of(sale1, sale2));

      when(vendingMachineDao.read(machine.getId())).thenReturn(machine);
      when(dao.findLastGenerated(machine)).then(invocation -> {
        Report oldReport = new Report();
        oldReport.setCreatedAt(Instant.parse("2022-04-02T10:15:30Z"));
        return Optional.of(oldReport);
      });

      ReportDTO report = controller.report(machine.getId());

      assertAll(() -> assertReportHasMachineInfo(report, machine),
          () -> assertThat(report.getTotalSaleAmount()).isEqualTo(sale2.getAmount()));
    }
  }

  VendingMachine buildMachine(final UUID id)
  {
    VendingMachine machine = new VendingMachine();
    machine.setId(id);
    machine.setType(ItemType.FOOD);
    machine.setSerialNumber("123456789");
    machine.setChangeMoneyStatus(ChangeSystemStatus.NORMAL);
    machine.setAddress("123 Fake street");
    machine.setLastIntervention(Instant.parse("2022-04-01T10:15:30Z"));
    machine.setPowerStatus(PowerStatus.ON);
    machine.setWorkingStatus(WorkingStatus.OK);
    machine.setTemperature(4);

    return machine;
  }

  private static void assertReportHasMachineInfo(final ReportDTO report, final VendingMachine machine)
  {
    assertThat(report).hasFieldOrPropertyWithValue("machineSerialNumber", machine.getSerialNumber())
      .hasFieldOrPropertyWithValue("mesuredTemperature", machine.getTemperature())
      .hasFieldOrPropertyWithValue("powerStatus", machine.getPowerStatus())
      .hasFieldOrPropertyWithValue("workingStatus", machine.getWorkingStatus())
      .hasFieldOrPropertyWithValue("rfidStatus", machine.getRfidStatus())
      .hasFieldOrPropertyWithValue("smartCardStatus", machine.getSmartCardStatus())
      .hasFieldOrPropertyWithValue("changeMoneyStatus", machine.getChangeMoneyStatus());
  }
}
