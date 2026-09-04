package me.dahiorus.project.vending.domain.machine.port;

import java.util.Optional;
import me.dahiorus.project.vending.domain.Creatable;
import me.dahiorus.project.vending.domain.Deletable;
import me.dahiorus.project.vending.domain.Findable;
import me.dahiorus.project.vending.domain.Searchable;
import me.dahiorus.project.vending.domain.Updatable;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineToUpdate;

public interface VendingMachineRepositoryPort
    extends Creatable<VendingMachine, VendingMachine>,
        Findable<VendingMachineId, VendingMachine>,
        Updatable<VendingMachineToUpdate, VendingMachine>,
        Deletable<VendingMachineId>,
        Searchable<VendingMachine, VendingMachine> {
  Optional<VendingMachine> findDuplicateOf(VendingMachine machine);
}
