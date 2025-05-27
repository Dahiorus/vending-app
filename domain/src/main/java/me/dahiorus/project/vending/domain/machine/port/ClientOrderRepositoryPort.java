package me.dahiorus.project.vending.domain.machine.port;

import java.time.LocalDateTime;
import java.util.List;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.machine.entity.ClientOrder;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;

public interface ClientOrderRepositoryPort {
  ClientOrder create(VendingMachineId vendingMachineId, ItemId itemId) throws ResourceNotFound;

  List<ClientOrder> findAllOfVendingMachine(VendingMachineId vendingMachineId)
      throws ResourceNotFound;

  List<ClientOrder> findAllOfVendingMachineSince(
      VendingMachineId vendingMachineId, LocalDateTime since) throws ResourceNotFound;
}
