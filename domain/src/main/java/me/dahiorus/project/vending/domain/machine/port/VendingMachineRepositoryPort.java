package me.dahiorus.project.vending.domain.machine.port;

import java.util.Optional;
import me.dahiorus.project.vending.domain.CreateSpi;
import me.dahiorus.project.vending.domain.DeleteSpi;
import me.dahiorus.project.vending.domain.FindSpi;
import me.dahiorus.project.vending.domain.SearchSpi;
import me.dahiorus.project.vending.domain.UpdateSpi;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineToUpdate;

public interface VendingMachineRepositoryPort
    extends CreateSpi<VendingMachine, VendingMachine>,
        FindSpi<VendingMachineId, VendingMachine>,
        UpdateSpi<VendingMachineToUpdate, VendingMachine>,
        DeleteSpi<VendingMachineId>,
        SearchSpi<VendingMachine, VendingMachine> {
  Optional<VendingMachine> findDuplicateOf(VendingMachine machine);
}
