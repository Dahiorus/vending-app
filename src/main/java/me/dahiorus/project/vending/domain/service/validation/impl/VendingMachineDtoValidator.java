package me.dahiorus.project.vending.domain.service.validation.impl;

import static me.dahiorus.project.vending.domain.service.validation.FieldValidationError.emptyOrNullValue;
import static me.dahiorus.project.vending.domain.service.validation.FieldValidationError.fieldError;
import static me.dahiorus.project.vending.domain.service.validation.FieldValidationError.notUniqueValue;
import static me.dahiorus.project.vending.domain.service.validation.ValidationError.getFullCode;

import java.util.Objects;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.dao.DAO;
import me.dahiorus.project.vending.domain.model.Address;
import me.dahiorus.project.vending.domain.model.Address_;
import me.dahiorus.project.vending.domain.model.PowerStatus;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.VendingMachine_;
import me.dahiorus.project.vending.domain.model.WorkingStatus;
import me.dahiorus.project.vending.domain.model.dto.VendingMachineDTO;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;

@Log4j2
@Component
public class VendingMachineDtoValidator extends DtoValidatorImpl<VendingMachine, VendingMachineDTO, DAO<VendingMachine>>
{
  @Autowired
  public VendingMachineDtoValidator(final DAO<VendingMachine> dao)
  {
    super(dao);
  }

  @Override
  public Logger getLogger()
  {
    return log;
  }

  @Override
  protected void doValidate(final VendingMachineDTO dto, final ValidationResults results)
  {
    // validate all mandatory fields
    rejectIfBlank(VendingMachine_.SERIAL_NUMBER, dto.getSerialNumber(), results);
    rejectIfInvalidLength(VendingMachine_.SERIAL_NUMBER, dto.getSerialNumber(), 255, results);
    rejectIfEmpty(VendingMachine_.TYPE, dto.getType(), results);
    rejectIfEmpty(VendingMachine_.POWER_STATUS, dto.getPowerStatus(), results);
    validateAddress(dto, results);

    // validate the serial number is unique
    checkSerialNumberUniqueness(results, dto);

    // validate working status consistency with power status
    checkWorkingStatus(results, dto);
  }

  private void validateAddress(final VendingMachineDTO dto, final ValidationResults results)
  {
    Address address = dto.getAddress();
    rejectIfBlank(Address_.ADDRESS, address.getStreetAddress(), results);
    rejectIfInvalidLength(Address_.ADDRESS, address.getStreetAddress(), 255, results);
    rejectIfInvalidLength(Address_.PLACE, address.getPlace(), 255, results);
  }

  private void checkSerialNumberUniqueness(final ValidationResults validationResults, final VendingMachineDTO dto)
  {
    String serialNumber = dto.getSerialNumber();

    if (!validationResults.hasFieldError(VendingMachine_.SERIAL_NUMBER))
    {
      dao.findOne((root, query, cb) -> cb.equal(root.get(VendingMachine_.serialNumber), serialNumber))
        .ifPresent(other -> {
          if (!Objects.equals(dto.getId(), other.getId()))
          {
            validationResults.addError(notUniqueValue(VendingMachine_.SERIAL_NUMBER, serialNumber));
          }
        });
    }
  }

  private static void checkWorkingStatus(final ValidationResults validationResults, final VendingMachineDTO dto)
  {
    final WorkingStatus workingStatus = dto.getWorkingStatus();
    final PowerStatus powerStatus = dto.getPowerStatus();

    if (workingStatus != null && powerStatus == PowerStatus.OFF)
    {
      validationResults.addError(fieldError(VendingMachine_.WORKING_STATUS,
          getFullCode("vending_machine.working_status_consistency"),
          "An OFF vending machine cannot have a working status", workingStatus));
    }
    else if (workingStatus == null && powerStatus == PowerStatus.ON)
    {
      validationResults.addError(emptyOrNullValue(VendingMachine_.WORKING_STATUS));
    }
  }
}
