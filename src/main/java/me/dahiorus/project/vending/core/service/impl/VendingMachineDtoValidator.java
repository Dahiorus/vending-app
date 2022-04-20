package me.dahiorus.project.vending.core.service.impl;

import static org.springframework.validation.ValidationUtils.rejectIfEmpty;
import static org.springframework.validation.ValidationUtils.rejectIfEmptyOrWhitespace;

import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import me.dahiorus.project.vending.core.dao.AbstractDAO;
import me.dahiorus.project.vending.core.model.PowerStatus;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.model.VendingMachine_;
import me.dahiorus.project.vending.core.model.WorkingStatus;
import me.dahiorus.project.vending.core.model.dto.VendingMachineDTO;

@Component
public class VendingMachineDtoValidator extends DtoValidator<VendingMachine, VendingMachineDTO>
{
  private static final Logger logger = LogManager.getLogger(VendingMachineDtoValidator.class);

  public VendingMachineDtoValidator(final AbstractDAO<VendingMachine> dao)
  {
    super(dao);
  }

  @Override
  protected Class<VendingMachineDTO> getSupportedClass()
  {
    return VendingMachineDTO.class;
  }

  @Override
  protected void doValidate(final VendingMachineDTO dto, final Errors errors)
  {
    final UUID id = dto.getId();

    // validate all mandatory fields
    rejectIfEmptyOrWhitespace(errors, VendingMachine_.SERIAL_NUMBER, CODE_FIELD_EMPTY,
        "The serial number is mandatory");
    rejectIfEmptyOrWhitespace(errors, VendingMachine_.ADDRESS, CODE_FIELD_EMPTY, "The address is mandatory");
    rejectIfEmpty(errors, VendingMachine_.TYPE, CODE_FIELD_EMPTY, "The type is mandatory");
    rejectIfEmpty(errors, VendingMachine_.POWER_STATUS, CODE_FIELD_EMPTY, "The power status is mandatory");

    // validate the serial number is unique
    checkSerialNumberUniqueness(errors, dto, id);

    // validate working status consistency with power status
    checkWorkingStatus(errors, dto);
  }

  private void checkSerialNumberUniqueness(final Errors errors, final VendingMachineDTO dto, final UUID id)
  {
    final String serialNumber = dto.getSerialNumber();

    if (StringUtils.isEmpty(serialNumber))
    {
      return;
    }

    dao.findOne((root, query, cb) -> cb.equal(root.get(VendingMachine_.serialNumber), serialNumber))
      .ifPresent(other -> {
        if (!Objects.equals(id, other.getId()))
        {
          errors.rejectValue(VendingMachine_.SERIAL_NUMBER,
              "validation.constraints.vending_machine.unique_serial_number",
              "The serial number must be unique");
        }
      });
  }

  private static void checkWorkingStatus(final Errors errors, final VendingMachineDTO dto)
  {
    final WorkingStatus workingStatus = dto.getWorkingStatus();
    final PowerStatus powerStatus = dto.getPowerStatus();

    if (workingStatus != null && powerStatus == PowerStatus.OFF)
    {
      errors.rejectValue(VendingMachine_.WORKING_STATUS,
          "validation.constraints.vending_machine.working_status_consistency",
          "An OFF vending machine cannot have a working status");
    }
    else if (workingStatus == null && powerStatus == PowerStatus.ON)
    {
      errors.rejectValue(VendingMachine_.WORKING_STATUS,
          "validation.constraints.vending_machine.working_status_mandatory",
          "An ON vending machine must have a working status");
    }
  }

  @Override
  public Logger getLogger()
  {
    return logger;
  }
}
