package me.dahiorus.project.vending.domain.machine.entity;

import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.PowerStatus.POWER_OFF;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.PowerStatus.POWER_ON;
import static me.dahiorus.project.vending.fixture.VendingMachineStatusFixture.aVendingMachineStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

import java.util.stream.Stream;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.CardSystemStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.ChangeSystemStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.PowerStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.WorkingStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class VendingMachineStatusTest {
  public static Stream<Arguments> expectedIsPoweredResult() {
    return Stream.of(Arguments.of(POWER_ON, true), Arguments.of(POWER_OFF, false));
  }

  @ParameterizedTest
  @MethodSource("expectedIsPoweredResult")
  void should_be_powered_according_to_status(PowerStatus powerStatus, boolean expected) {
    var machineStatus = aVendingMachineStatus().powerStatus(powerStatus).build();

    var result = machineStatus.isPowered();

    assertThat(result).isEqualTo(expected);
  }

  @Nested
  class IsWorking {
    @Test
    void should_be_working_given_working_status_OK() {
      var machineStatus =
          aVendingMachineStatus()
              .powerStatus(POWER_ON)
              .workingStatus(WorkingStatus.WORKING)
              .build();

      var result = machineStatus.isWorking();

      assertThat(result).isTrue();
    }

    @ParameterizedTest
    @EnumSource(mode = EXCLUDE, names = "WORKING")
    void should_not_be_working_given_working_status_not_WORKING(WorkingStatus workingStatus) {
      var machineStatus =
          aVendingMachineStatus().powerStatus(POWER_ON).workingStatus(workingStatus).build();

      var result = machineStatus.isWorking();

      assertThat(result).isFalse();
    }

    @Test
    void should_not_be_working_when_not_powered() {
      var machineStatus =
          aVendingMachineStatus()
              .powerStatus(POWER_OFF)
              .workingStatus(WorkingStatus.WORKING)
              .build();

      var result = machineStatus.isWorking();

      assertThat(result).isFalse();
    }
  }

  @Nested
  class IsAllSystemClear {
    @Test
    void should_be_all_system_clear() {
      var machineStatus =
          aVendingMachineStatus()
              .powerStatus(POWER_ON)
              .workingStatus(WorkingStatus.WORKING)
              .rfidStatus(CardSystemStatus.OK)
              .smartCardStatus(CardSystemStatus.OK)
              .changeMoneyStatus(ChangeSystemStatus.NORMAL)
              .build();

      var result = machineStatus.isAllSystemClear();

      assertThat(result).isTrue();
    }

    @Test
    void should_not_be_all_system_clear_when_not_working() {
      var machineStatus =
          aVendingMachineStatus()
              .powerStatus(POWER_ON)
              .workingStatus(WorkingStatus.ALERT)
              .rfidStatus(CardSystemStatus.OK)
              .smartCardStatus(CardSystemStatus.OK)
              .changeMoneyStatus(ChangeSystemStatus.NORMAL)
              .build();

      var result = machineStatus.isAllSystemClear();

      assertThat(result).isFalse();
    }

    @Test
    void should_not_be_all_system_clear_when_rfid_status_is_not_normal() {
      var machineStatus =
          aVendingMachineStatus()
              .powerStatus(POWER_ON)
              .workingStatus(WorkingStatus.WORKING)
              .rfidStatus(CardSystemStatus.FAILED)
              .smartCardStatus(CardSystemStatus.OK)
              .changeMoneyStatus(ChangeSystemStatus.NORMAL)
              .build();

      var result = machineStatus.isAllSystemClear();

      assertThat(result).isFalse();
    }

    @Test
    void should_not_be_all_system_clear_when_smart_card_status_is_not_normal() {
      var machineStatus =
          aVendingMachineStatus()
              .powerStatus(POWER_ON)
              .workingStatus(WorkingStatus.WORKING)
              .rfidStatus(CardSystemStatus.OK)
              .smartCardStatus(CardSystemStatus.FAILED)
              .changeMoneyStatus(ChangeSystemStatus.NORMAL)
              .build();

      var result = machineStatus.isAllSystemClear();

      assertThat(result).isFalse();
    }

    @Test
    void should_not_be_all_system_clear_when_change_money_status_is_not_normal() {
      var machineStatus =
          aVendingMachineStatus()
              .powerStatus(POWER_ON)
              .workingStatus(WorkingStatus.WORKING)
              .rfidStatus(CardSystemStatus.OK)
              .smartCardStatus(CardSystemStatus.OK)
              .changeMoneyStatus(ChangeSystemStatus.EMPTY)
              .build();

      var result = machineStatus.isAllSystemClear();

      assertThat(result).isFalse();
    }
  }
}
