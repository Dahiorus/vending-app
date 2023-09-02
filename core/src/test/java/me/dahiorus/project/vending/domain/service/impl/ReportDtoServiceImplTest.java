package me.dahiorus.project.vending.domain.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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

import me.dahiorus.project.vending.domain.dao.ReportDao;
import me.dahiorus.project.vending.domain.dao.VendingMachineDao;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.model.ChangeSystemStatus;
import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.ItemType;
import me.dahiorus.project.vending.domain.model.PowerStatus;
import me.dahiorus.project.vending.domain.model.Report;
import me.dahiorus.project.vending.domain.model.Sale;
import me.dahiorus.project.vending.domain.model.Stock;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.WorkingStatus;
import me.dahiorus.project.vending.domain.model.dto.ReportDto;
import me.dahiorus.project.vending.util.VendingMachineBuilder;

@ExtendWith(MockitoExtension.class)
class ReportDtoServiceImplTest
{
  @Mock
  ReportDao dao;

  @Mock
  VendingMachineDao vendingMachineDao;

  ReportDtoServiceImpl dtoService;

  @Captor
  ArgumentCaptor<Report> reportArg;

  @BeforeEach
  void setUp()
  {
    when(dao.getDomainClass()).thenReturn(Report.class);
    dtoService = new ReportDtoServiceImpl(dao, new DtoMapperImpl(), vendingMachineDao);
  }

  @Test
  void createIsUnsupported()
  {
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> dtoService.create(new ReportDto()));
  }

  @Test
  void updateIsUnsupported()
  {
    assertThatExceptionOfType(UnsupportedOperationException.class)
      .isThrownBy(() -> dtoService.update(UUID.randomUUID(), new ReportDto()));
  }

  @Nested
  class ReportTests
  {
    @Test
    void reportUnkownMachine() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(vendingMachineDao.read(id)).thenThrow(new EntityNotFound(VendingMachine.class, id));

      assertThatExceptionOfType(EntityNotFound.class).isThrownBy(() -> dtoService.report(id));
      verify(dao, never()).save(any());
    }

    @Test
    void reportVendingMachine() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID());

      when(vendingMachineDao.read(machine.getId())).thenReturn(machine);
      mockSaveReport();

      ReportDto report = dtoService.report(machine.getId());

      assertReportHasMachineInfo(report, machine);
    }

    @Test
    void reportMachineWithStock() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID());
      Item item = new Item();
      item.setId(UUID.randomUUID());
      item.setName("Mars");
      item.setPrice(BigDecimal.valueOf(1.2));
      item.setType(machine.getType());
      Stock stock = Stock.fill(item, 12);
      stock.setId(UUID.randomUUID());
      machine.setStocks(List.of(stock));

      when(vendingMachineDao.read(machine.getId())).thenReturn(machine);
      mockSaveReport();

      ReportDto report = dtoService.report(machine.getId());

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
      sale1.setAmount(BigDecimal.valueOf(1.2));
      sale1.setMachine(machine);
      Sale sale2 = new Sale();
      sale2.setAmount(BigDecimal.valueOf(1.5));
      sale2.setMachine(machine);
      machine.setSales(List.of(sale1, sale2));

      when(vendingMachineDao.read(machine.getId())).thenReturn(machine);
      mockSaveReport();

      ReportDto report = dtoService.report(machine.getId());

      assertAll(() -> assertReportHasMachineInfo(report, machine),
        () -> assertThat(report.getTotalSaleAmount()).isEqualTo(sale1.getAmount()
          .add(sale2.getAmount())));
    }

    @Test
    void reportVendingMachineHavingOldReport() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID());
      Sale sale1 = new Sale();
      sale1.setAmount(BigDecimal.valueOf(1.2));
      sale1.setMachine(machine);
      sale1.setCreatedAt(Instant.parse("2022-04-01T10:15:30Z"));
      Sale sale2 = new Sale();
      sale2.setAmount(BigDecimal.valueOf(1.5));
      sale2.setMachine(machine);
      sale2.setCreatedAt(Instant.parse("2022-04-10T10:15:30Z"));
      machine.setSales(List.of(sale1, sale2));

      when(vendingMachineDao.read(machine.getId())).thenReturn(machine);
      when(dao.findLastGenerated(machine)).then(invocation -> {
        Report oldReport = new Report();
        oldReport.setCreatedAt(Instant.parse("2022-04-02T10:15:30Z"));
        return Optional.of(oldReport);
      });
      mockSaveReport();

      ReportDto report = dtoService.report(machine.getId());

      assertAll(() -> assertReportHasMachineInfo(report, machine),
        () -> assertThat(report.getTotalSaleAmount()).isEqualTo(sale2.getAmount()));
    }

    void mockSaveReport()
    {
      when(dao.save(any()))
        .then(invocation -> {
          Report arg = invocation.getArgument(0);
          arg.setId(UUID.randomUUID());
          arg.setCreatedAt(Instant.now());
          return arg;
        });
    }
  }

  VendingMachine buildMachine(final UUID id)
  {
    return VendingMachineBuilder.builder()
      .id(id)
      .itemType(ItemType.FOOD)
      .changeMoneyStatus(ChangeSystemStatus.NORMAL)
      .powerStatus(PowerStatus.ON)
      .workingStatus(WorkingStatus.OK)
      .serialNumber("123456789")
      .address("123 Fake street")
      .lastIntervention(Instant.parse("2022-04-01T10:15:30Z"))
      .temperature(4)
      .build();
  }

  private static void assertReportHasMachineInfo(final ReportDto report, final VendingMachine machine)
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
