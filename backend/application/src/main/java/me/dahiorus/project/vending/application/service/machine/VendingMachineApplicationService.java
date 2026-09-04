package me.dahiorus.project.vending.application.service.machine;

import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineToUpdate;
import me.dahiorus.project.vending.domain.machine.port.VendingMachineApiPort;
import me.dahiorus.project.vending.domain.machine.port.VendingMachineRepositoryPort;
import me.dahiorus.project.vending.domain.pagination.entity.Filter;
import me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher;
import me.dahiorus.project.vending.domain.pagination.entity.PageResult;
import me.dahiorus.project.vending.domain.pagination.entity.Pagination;
import me.dahiorus.project.vending.domain.pagination.entity.Total;
import me.dahiorus.project.vending.domain.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VendingMachineApplicationService implements VendingMachineApiPort {
  private final VendingMachineRepositoryPort vendingMachineRepository;
  private final Validator<VendingMachine> vendingMachineValidator;

  public VendingMachineApplicationService(
      VendingMachineRepositoryPort vendingMachineRepository,
      Validator<VendingMachine> vendingMachineValidator) {
    this.vendingMachineRepository = vendingMachineRepository;
    this.vendingMachineValidator = vendingMachineValidator;
  }

  @Override
  public VendingMachine create(VendingMachine vendingMachineToCreate) {
    vendingMachineValidator.validate(vendingMachineToCreate);
    return vendingMachineRepository.create(vendingMachineToCreate);
  }

  @Override
  public VendingMachine read(VendingMachineId id) throws ResourceNotFound {
    return vendingMachineRepository.find(id).orElseThrow(() -> new ResourceNotFound(id));
  }

  @Override
  public VendingMachine update(VendingMachineToUpdate vendingMachineToUpdate) {
    return vendingMachineRepository.update(vendingMachineToUpdate);
  }

  @Override
  public void delete(VendingMachineId id) {
    vendingMachineRepository.delete(id);
  }

  @Override
  public PageResult<VendingMachine> search(
      Pagination pagination, VendingMachine example, FilterMatcher filterMatcher) {
    var filter = new Filter<>(example, filterMatcher);
    var vendingMachines = vendingMachineRepository.search(pagination, filter);
    var count = vendingMachineRepository.count(filter);

    return new PageResult<>(vendingMachines, pagination, new Total(count));
  }
}
