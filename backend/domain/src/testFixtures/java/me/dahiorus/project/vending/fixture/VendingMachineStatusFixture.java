package me.dahiorus.project.vending.fixture;

import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.defaultStatus;

import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.CardSystemStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.ChangeSystemStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.PowerStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.Temperature;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.WorkingStatus;

public class VendingMachineStatusFixture {

  public static Builder aVendingMachineStatus() {
    var defaultStatus = defaultStatus();

    return new Builder()
        .temperature(defaultStatus.temperature().value())
        .powerStatus(defaultStatus.powerStatus())
        .workingStatus(defaultStatus.workingStatus())
        .rfidStatus(defaultStatus.rfidStatus())
        .smartCardStatus(defaultStatus.smartCardStatus())
        .changeMoneyStatus(defaultStatus.changeMoneyStatus());
  }

  public static class Builder {
    private Temperature temperature;
    private PowerStatus powerStatus;
    private WorkingStatus workingStatus;
    private CardSystemStatus rfidStatus;
    private CardSystemStatus smartCardStatus;
    private ChangeSystemStatus changeMoneyStatus;

    public Builder temperature(Integer temperature) {
      this.temperature = Temperature.of(temperature);
      return this;
    }

    public Builder powerStatus(PowerStatus powerStatus) {
      this.powerStatus = powerStatus;
      return this;
    }

    public Builder workingStatus(WorkingStatus workingStatus) {
      this.workingStatus = workingStatus;
      return this;
    }

    public Builder rfidStatus(CardSystemStatus rfidStatus) {
      this.rfidStatus = rfidStatus;
      return this;
    }

    public Builder smartCardStatus(CardSystemStatus smartCardStatus) {
      this.smartCardStatus = smartCardStatus;
      return this;
    }

    public Builder changeMoneyStatus(ChangeSystemStatus changeMoneyStatus) {
      this.changeMoneyStatus = changeMoneyStatus;
      return this;
    }

    public VendingMachineStatus build() {
      return new VendingMachineStatus(
          temperature, powerStatus, workingStatus, rfidStatus, smartCardStatus, changeMoneyStatus);
    }
  }
}
