package me.dahiorus.project.vending.domain.machine.usecase;

import static java.time.LocalDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.util.Optional.empty;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.CardSystemStatus.FAILED;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.CardSystemStatus.OK;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.ChangeSystemStatus.FULL;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.ChangeSystemStatus.NORMAL;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.PowerStatus.POWER_ON;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.WorkingStatus.ALERT;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.WorkingStatus.WORKING;
import static me.dahiorus.project.vending.fixture.VendingMachineFixture.aVendingMachine;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.doReturn;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.Temperature;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineToUpdate;
import me.dahiorus.project.vending.domain.machine.port.VendingMachineRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RepairVendingMachineTest {
  @Mock(strictness = LENIENT)
  Clock clock;

  @Mock VendingMachineRepositoryPort vendingMachineRepository;
  @InjectMocks RepairVendingMachine repairVendingMachine;

  @Captor ArgumentCaptor<VendingMachineToUpdate> vendingMachineToUpdateCaptor;

  @BeforeEach
  void setUpClock() {
    var fixedClock = Clock.fixed(Instant.now(), systemDefault());
    doReturn(fixedClock.instant()).when(clock).instant();
    doReturn(fixedClock.getZone()).when(clock).getZone();
  }

  @Test
  void should_reset_all_statuses_when_repair_vending_machine() {
    // Given
    var vendingMachine =
        aVendingMachine()
            .status(new VendingMachineStatus(Temperature.of(4), POWER_ON, ALERT, FAILED, OK, FULL))
            .build();

    given(vendingMachineRepository.find(vendingMachine.id()))
        .willReturn(Optional.of(vendingMachine));

    // When
    var result = repairVendingMachine.execute(vendingMachine.id());

    // Then
    assertThat(result.isAllSystemClear()).isTrue();
    assertThat(result.lastIntervention()).isEqualTo(now(clock));
  }

  @Test
  void should_do_nothing_when_vending_machine_is_all_system_clear() {
    // Given
    var vendingMachine =
        aVendingMachine()
            .status(new VendingMachineStatus(Temperature.of(4), POWER_ON, WORKING, OK, OK, NORMAL))
            .lastIntervention(null)
            .build();
    assertThat(vendingMachine.isAllSystemClear()).isTrue();

    given(vendingMachineRepository.find(vendingMachine.id()))
        .willReturn(Optional.of(vendingMachine));

    // When
    var result = repairVendingMachine.execute(vendingMachine.id());

    // Then
    assertThat(result.isAllSystemClear()).isTrue();
    assertThat(result.lastIntervention()).isNull();
    then(vendingMachineRepository).shouldHaveNoMoreInteractions();
  }

  @Test
  void should_throw_exception_when_vending_machine_not_found() {
    // Given
    var vendingMachine = aVendingMachine().build();

    given(vendingMachineRepository.find(vendingMachine.id())).willReturn(empty());

    // When / Then
    assertThatThrownBy(() -> repairVendingMachine.execute(vendingMachine.id()))
        .isInstanceOf(ResourceNotFound.class);
  }
}
