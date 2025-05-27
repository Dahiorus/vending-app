package me.dahiorus.project.vending.domain.machine.usecase;

import static me.dahiorus.project.vending.domain.validation.ValidationResults.validationResults;

import me.dahiorus.project.vending.domain.documentation.DomainService;
import me.dahiorus.project.vending.domain.exception.InvalidBusinessObject;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;
import me.dahiorus.project.vending.domain.machine.port.VendingMachineRepositoryPort;
import me.dahiorus.project.vending.domain.validation.ObjectValidationError;
import me.dahiorus.project.vending.domain.validation.ValidationResults;
import me.dahiorus.project.vending.domain.validation.Validator;

@DomainService
public class VendingMachineValidator implements Validator<VendingMachine> {
  private final VendingMachineRepositoryPort vendingMachineRepository;

  public VendingMachineValidator(VendingMachineRepositoryPort vendingMachineRepository) {
    this.vendingMachineRepository = vendingMachineRepository;
  }

  @Override
  public ValidationResults buildValidation(VendingMachine data) throws InvalidBusinessObject {
    var validationResults = validationResults();
    vendingMachineRepository
        .findDuplicateOf(data)
        .map(ObjectValidationError::notUnique)
        .ifPresent(validationResults::addError);

    return validationResults;
  }
}
