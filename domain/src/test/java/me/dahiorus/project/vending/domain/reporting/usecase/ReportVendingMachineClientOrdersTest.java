package me.dahiorus.project.vending.domain.reporting.usecase;

import static java.time.LocalDateTime.now;
import static java.time.Period.ofWeeks;
import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static me.dahiorus.project.vending.fixture.ClientOrderFixture.aClientOrder;
import static me.dahiorus.project.vending.fixture.VendingMachineFixture.aVendingMachine;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.machine.entity.SerialNumber;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.port.ClientOrderRepositoryPort;
import me.dahiorus.project.vending.domain.machine.port.VendingMachineRepositoryPort;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReport;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReport.ReportedClientOrder;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReportId;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReportToCreate;
import me.dahiorus.project.vending.domain.reporting.port.VendingMachineClientOrdersReportRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportVendingMachineClientOrdersTest {

  @Mock VendingMachineRepositoryPort vendingMachineRepository;
  @Mock ClientOrderRepositoryPort clientOrderRepository;
  @Mock VendingMachineClientOrdersReportRepositoryPort vendingMachineClientOrdersReportRepository;
  @InjectMocks ReportVendingMachineClientOrders reportVendingMachineClientOrders;

  @Test
  void should_report_vending_machine_client_orders_with_all_orders() {
    // Given
    var vendingMachine = aVendingMachine().serialNumber("VM-1234").build();
    given(vendingMachineRepository.find(vendingMachine.id()))
        .willReturn(Optional.of(vendingMachine));
    var vendingMachineClientOrders =
        List.of(
            aClientOrder(
                vendingMachine, "Lays 100g", BigDecimal.valueOf(1.5), now().minus(ofWeeks(3))),
            aClientOrder(
                vendingMachine, "Lays 100g", BigDecimal.valueOf(1.5), now().minus(ofWeeks(3))));
    given(clientOrderRepository.findAllOfVendingMachine(vendingMachine.id()))
        .willReturn(vendingMachineClientOrders);

    var expectedClientOrdersReport =
        new VendingMachineClientOrdersReport(
            new VendingMachineClientOrdersReportId(randomUUID()),
            SerialNumber.of("VM-1234"),
            vendingMachineClientOrders.stream().map(ReportedClientOrder::from).toList(),
            now());
    given(
            vendingMachineClientOrdersReportRepository.create(
                new VendingMachineClientOrdersReportToCreate(
                    vendingMachine, vendingMachineClientOrders)))
        .willReturn(expectedClientOrdersReport);

    // When
    var result = reportVendingMachineClientOrders.execute(vendingMachine.id());

    // Then
    assertThat(result).isEqualTo(expectedClientOrdersReport);
    then(clientOrderRepository)
        .should(never())
        .findAllOfVendingMachineSince(eq(vendingMachine.id()), any());
  }

  @Test
  void should_report_vending_machine_client_orders_with_orders_since_last_report() {
    // Given
    var vendingMachine = aVendingMachine().serialNumber("VM-1234").build();
    given(vendingMachineRepository.find(vendingMachine.id()))
        .willReturn(Optional.of(vendingMachine));

    var lastReport =
        new VendingMachineClientOrdersReport(
            new VendingMachineClientOrdersReportId(randomUUID()),
            SerialNumber.of("VM-1234"),
            List.of(),
            now().minus(ofWeeks(2)));
    given(vendingMachineClientOrdersReportRepository.findLastGeneratedOf(vendingMachine))
        .willReturn(Optional.of(lastReport));

    var vendingMachineClientOrders =
        List.of(
            aClientOrder(
                vendingMachine, "Lays 100g", BigDecimal.valueOf(1.5), now().minus(ofWeeks(3))),
            aClientOrder(
                vendingMachine, "Lays 100g", BigDecimal.valueOf(1.5), now().minus(ofWeeks(3))));
    given(
            clientOrderRepository.findAllOfVendingMachineSince(
                vendingMachine.id(), lastReport.reportedAt()))
        .willReturn(vendingMachineClientOrders);

    var expectedClientOrdersReport =
        new VendingMachineClientOrdersReport(
            new VendingMachineClientOrdersReportId(randomUUID()),
            SerialNumber.of("VM-1234"),
            vendingMachineClientOrders.stream().map(ReportedClientOrder::from).toList(),
            now());
    given(
            vendingMachineClientOrdersReportRepository.create(
                new VendingMachineClientOrdersReportToCreate(
                    vendingMachine, vendingMachineClientOrders)))
        .willReturn(expectedClientOrdersReport);

    // When
    var result = reportVendingMachineClientOrders.execute(vendingMachine.id());

    // Then
    assertThat(result).isEqualTo(expectedClientOrdersReport);
    then(clientOrderRepository).should(never()).findAllOfVendingMachine(vendingMachine.id());
  }

  @Test
  void should_throw_exception_when_vending_machine_not_found() {
    // Given
    var vendingMachineId = new VendingMachineId(randomUUID());
    given(vendingMachineRepository.find(vendingMachineId)).willReturn(empty());

    // When & Then
    assertThatThrownBy(() -> reportVendingMachineClientOrders.execute(vendingMachineId))
        .isInstanceOf(ResourceNotFound.class)
        .hasMessageContaining("Resource not found with ID: " + vendingMachineId);
    then(clientOrderRepository).shouldHaveNoInteractions();
    then(vendingMachineClientOrdersReportRepository).shouldHaveNoInteractions();
  }
}
