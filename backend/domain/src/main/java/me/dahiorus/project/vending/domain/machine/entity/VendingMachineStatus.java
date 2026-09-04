package me.dahiorus.project.vending.domain.machine.entity;

import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.CardSystemStatus.OK;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.ChangeSystemStatus.NORMAL;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.PowerStatus.POWER_OFF;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.PowerStatus.POWER_ON;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.WorkingStatus.WORKING;

import java.io.Serializable;

public record VendingMachineStatus(
    Temperature temperature,
    PowerStatus powerStatus,
    WorkingStatus workingStatus,
    CardSystemStatus rfidStatus,
    CardSystemStatus smartCardStatus,
    ChangeSystemStatus changeMoneyStatus)
    implements Serializable {

  public static final Temperature DEFAULT_TEMPERATURE = Temperature.of(4);

  public static VendingMachineStatus defaultStatus() {
    return new VendingMachineStatus(DEFAULT_TEMPERATURE, POWER_OFF, WORKING, OK, OK, NORMAL);
  }

  public boolean isPowered() {
    return powerStatus == POWER_ON;
  }

  public boolean isWorking() {
    return isPowered() && workingStatus == WORKING;
  }

  public boolean isAllSystemClear() {
    return isWorking() && rfidStatus == OK && smartCardStatus == OK && changeMoneyStatus == NORMAL;
  }

  public record Temperature(Integer value) implements Serializable {
    public static Temperature of(Integer value) {
      return new Temperature(value);
    }

    public Temperature {
      if (value == null) {
        throw new IllegalArgumentException("Temperature value must not be null");
      }
    }
  }

  public enum PowerStatus {
    POWER_ON,
    POWER_OFF
  }

  public enum WorkingStatus {
    WORKING,
    WARNING,
    ERROR,
    ALERT
  }

  public enum CardSystemStatus {
    FAILED,
    OK
  }

  public enum ChangeSystemStatus {
    FULL,
    EMPTY,
    NORMAL
  }
}
