package me.dahiorus.project.vending.domain.reporting.usecase;

import static java.time.LocalDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.util.Optional.empty;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.CardSystemStatus.FAILED;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.CardSystemStatus.OK;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.ChangeSystemStatus.FULL;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.PowerStatus.POWER_ON;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.WorkingStatus.WARNING;
import static me.dahiorus.project.vending.fixture.VendingMachineFixture.aVendingMachine;
import static me.dahiorus.project.vending.fixture.VendingMachineStatusFixture.aVendingMachineStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.machine.port.VendingMachineRepositoryPort;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStatusReport;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStatusReportId;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStatusReportToCreate;
import me.dahiorus.project.vending.domain.reporting.port.VendingMachineStatusReportRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportVendingMachineStatusTest {
  @Mock VendingMachineRepositoryPort vendingMachineRepository;
  @Mock VendingMachineStatusReportRepositoryPort vendingMachineStatusReportRepository;
  @InjectMocks ReportVendingMachineStatus reportVendingMachineStatus;

  @Test
  void should_report_status_of_given_vending_machine() {
    var vendingMachine =
        aVendingMachine()
            .status(
                aVendingMachineStatus()
                    .powerStatus(POWER_ON)
                    .workingStatus(WARNING)
                    .rfidStatus(OK)
                    .smartCardStatus(FAILED)
                    .changeMoneyStatus(FULL)
                    .temperature(4)
                    .build())
            .lastIntervention(LocalDateTime.of(2025, 6, 2, 12, 15, 0))
            .build();
    given(vendingMachineRepository.find(vendingMachine.id()))
        .willReturn(Optional.of(vendingMachine));

    var expectedStatusReport =
        new VendingMachineStatusReport(
            new VendingMachineStatusReportId(UUID.randomUUID()),
            vendingMachine.serialNumber(),
            vendingMachine.lastIntervention(),
            vendingMachine.status(),
            now(systemDefault()));
    given(
            vendingMachineStatusReportRepository.create(
                new VendingMachineStatusReportToCreate(
                    vendingMachine.serialNumber(),
                    vendingMachine.lastIntervention(),
                    vendingMachine.status())))
        .willReturn(expectedStatusReport);

    var result = reportVendingMachineStatus.execute(vendingMachine.id());

    assertThat(result).isEqualTo(expectedStatusReport);
  }

  @Test
  void should_throw_exception_when_vending_machine_not_found() {
    var vendingMachineId = aVendingMachine().build().id();
    given(vendingMachineRepository.find(vendingMachineId)).willReturn(empty());

    assertThatThrownBy(() -> reportVendingMachineStatus.execute(vendingMachineId))
        .isInstanceOf(ResourceNotFound.class);
    then(vendingMachineStatusReportRepository).shouldHaveNoInteractions();
  }
}
