package me.dahiorus.project.vending.domain.machine.usecase;

import static java.time.LocalDateTime.now;

import java.time.Clock;
import me.dahiorus.project.vending.domain.documentation.DomainService;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.port.VendingMachineRepositoryPort;

@DomainService
public class RepairVendingMachine {
  private final VendingMachineRepositoryPort vendingMachineRepository;
  private final Clock clock;

  public RepairVendingMachine(VendingMachineRepositoryPort vendingMachineRepository, Clock clock) {
    this.vendingMachineRepository = vendingMachineRepository;
    this.clock = clock;
  }

  public VendingMachine execute(VendingMachineId id) throws ResourceNotFound {
    var vendingMachineToRepair =
        vendingMachineRepository.find(id).orElseThrow(() -> new ResourceNotFound(id));

    if (vendingMachineToRepair.isAllSystemClear()) {
      return vendingMachineToRepair;
    }

    var resetMachine = vendingMachineToRepair.resetAllStatuses().markIntervention(now(clock));
    vendingMachineRepository.update(resetMachine.toUpdate());

    return resetMachine;
  }
}
