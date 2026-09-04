package me.dahiorus.project.vending.domain.machine.port;

import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineToUpdate;
import me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher;
import me.dahiorus.project.vending.domain.pagination.entity.PageResult;
import me.dahiorus.project.vending.domain.pagination.entity.Pagination;

public interface VendingMachineApiPort {
  VendingMachine create(VendingMachine vendingMachineToCreate);

  VendingMachine read(VendingMachineId id) throws ResourceNotFound;

  VendingMachine update(VendingMachineToUpdate vendingMachineToUpdate);

  void delete(VendingMachineId id);

  PageResult<VendingMachine> search(
      Pagination pagination, VendingMachine example, FilterMatcher filterMatcher);
}
