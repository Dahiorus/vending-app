package me.dahiorus.project.vending.domain.reporting.usecase;

import static java.time.LocalDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.util.stream.Collectors.toSet;
import static me.dahiorus.project.vending.domain.item.entity.ItemType.SNACK;
import static me.dahiorus.project.vending.fixture.ItemFixture.aSnack;
import static me.dahiorus.project.vending.fixture.ItemQuantityFixture.itemQuantity;
import static me.dahiorus.project.vending.fixture.VendingMachineFixture.aVendingMachine;
import static me.dahiorus.project.vending.fixture.VendingMachineStocksFixture.emptyStock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import java.util.UUID;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.machine.entity.SerialNumber;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.port.VendingMachineRepositoryPort;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStockReport;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStockReport.ReportedStockEntry;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStockReportId;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStockReportToCreate;
import me.dahiorus.project.vending.domain.reporting.port.VendingMachineStockReportRepositoryPort;
import me.dahiorus.project.vending.domain.stock.port.VendingMachineStockRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportVendingMachineStockTest {
 @Mock VendingMachineRepositoryPort vendingMachineRepository;
  @Mock VendingMachineStockRepositoryPort vendingMachineStockRepository;
  @Mock VendingMachineStockReportRepositoryPort vendingMachineStockReportRepository;
  @InjectMocks ReportVendingMachineStock reportVendingMachineStock;

  @Test
  void should_report_stock_of_given_vending_machine() {
    // Given
    var vendingMachine =
        aVendingMachine()
            .id(new VendingMachineId(UUID.randomUUID()))
            .itemType(SNACK)
            .serialNumber("VM-1234")
            .build();
    given(vendingMachineRepository.find(vendingMachine.id()))
        .willReturn(Optional.of(vendingMachine));

    var vendingMachineStock =
        emptyStock()
            .addStock(itemQuantity(aSnack("Lays 100g", 1.8), 10))
            .addStock(itemQuantity(aSnack("Kinder Bueno", 2.1), 8));
    given(vendingMachineStockRepository.find(vendingMachine.id()))
        .willReturn(Optional.of(vendingMachineStock));

    var expectedStockReport =
        new VendingMachineStockReport(
            new VendingMachineStockReportId(UUID.randomUUID()),
            SerialNumber.of("VM-1234"),
            vendingMachineStock.stream().map(ReportedStockEntry::from).collect(toSet()),
            now(systemDefault()));
    given(
            vendingMachineStockReportRepository.create(
                new VendingMachineStockReportToCreate(
                    SerialNumber.of("VM-1234"), vendingMachineStock)))
        .willReturn(expectedStockReport);

    // When
    var result = reportVendingMachineStock.execute(vendingMachine.id());

    // Then
    assertThat(result).usingRecursiveComparison().isEqualTo(expectedStockReport);
  }

  @Test
  void should_throw_exception_when_vending_machine_not_found() {
    // Given
    var vendingMachineId = new VendingMachineId(UUID.randomUUID());
    given(vendingMachineRepository.find(vendingMachineId)).willReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> reportVendingMachineStock.execute(vendingMachineId))
        .isInstanceOf(ResourceNotFound.class)
        .hasMessage("Resource not found with ID: " + vendingMachineId);
  }

  @Test
  void should_throw_exception_when_vending_machine_stock_not_found() {
    // Given
    var vendingMachineId = new VendingMachineId(UUID.randomUUID());
    var vendingMachine = aVendingMachine().id(vendingMachineId).serialNumber("VM-1234").build();
    given(vendingMachineRepository.find(vendingMachineId)).willReturn(Optional.of(vendingMachine));
    given(vendingMachineStockRepository.find(vendingMachineId)).willReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> reportVendingMachineStock.execute(vendingMachineId))
        .isInstanceOf(ResourceNotFound.class)
        .hasMessage("Resource not found with ID: " + vendingMachineId);
  }
}
