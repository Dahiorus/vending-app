package me.dahiorus.project.vending.core.service.validation.impl;

import static com.dahiorus.project.vending.util.TestUtils.anySpec;
import static com.dahiorus.project.vending.util.TestUtils.assertHasExactlyFieldErrors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.dahiorus.project.vending.core.dao.ModelDaos.VendingMachineDAO;
import me.dahiorus.project.vending.core.model.ItemType;
import me.dahiorus.project.vending.core.model.PowerStatus;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.model.WorkingStatus;
import me.dahiorus.project.vending.core.model.dto.VendingMachineDTO;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;

@ExtendWith(MockitoExtension.class)
class VendingMachineDtoValidatorTest
{
  @Mock
  VendingMachineDAO dao;

  VendingMachineDtoValidator validator;

  VendingMachineDTO dto;

  @BeforeEach
  void setUp() throws Exception
  {
    validator = new VendingMachineDtoValidator(dao);
  }

  VendingMachineDTO buildDto(final String serialNumber, final String address, final ItemType type,
      final PowerStatus powerStatus, final WorkingStatus workingStatus)
  {
    VendingMachineDTO dto = new VendingMachineDTO();
    dto.setSerialNumber(serialNumber);
    dto.setAddress(address);
    dto.setType(type);
    dto.setPowerStatus(powerStatus);
    dto.setWorkingStatus(workingStatus);

    return dto;
  }

  @Test
  void dtoIsValid()
  {
    dto = buildDto("123456789", "1 Fake Street", ItemType.FOOD, PowerStatus.ON, WorkingStatus.OK);

    ValidationResults results = validator.validate(dto);

    assertThat(results.hasError()).as("No error on %s", dto)
      .isFalse();
  }

  @ParameterizedTest(name = "Blank serial number [{0}] is not valid")
  @NullAndEmptySource
  @ValueSource(strings = "  ")
  void serialNumberIsMandatory(final String serialNumber)
  {
    dto = buildDto(serialNumber, "1 Fake Street", ItemType.FOOD, PowerStatus.ON, WorkingStatus.OK);

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "serialNumber", "validation.constraints.empty_value");
  }

  @Test
  void serialNumberIsUnique()
  {
    dto = buildDto("123456789", "1 Fake Street", ItemType.FOOD, PowerStatus.ON, WorkingStatus.OK);
    VendingMachine duplicate = new VendingMachine();
    duplicate.setId(UUID.randomUUID());
    duplicate.setSerialNumber(dto.getSerialNumber());
    when(dao.findOne(anySpec())).thenReturn(Optional.of(duplicate));

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "serialNumber", "validation.constraints.not_unique");
  }

  @ParameterizedTest(name = "Blank serial number [{0}] is not valid")
  @NullAndEmptySource
  @ValueSource(strings = "  ")
  void addressIsMandatory(final String address)
  {
    dto = buildDto("123456789", address, ItemType.FOOD, PowerStatus.ON, WorkingStatus.OK);

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "address", "validation.constraints.empty_value");
  }

  @Test
  void typeIsMandatory()
  {
    dto = buildDto("123456789", "123 rue Sésame", null, PowerStatus.ON, WorkingStatus.OK);

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "type", "validation.constraints.empty_value");
  }

  @Test
  void powerStatusIsMandatory()
  {
    dto = buildDto("123456789", "123 rue Sésame", ItemType.COLD_BAVERAGE, null, null);

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "powerStatus", "validation.constraints.empty_value");
  }

  @Test
  void powerOffMachineCannotHaveWorkingStatus()
  {
    dto = buildDto("123456789", "123 rue Sésame", ItemType.COLD_BAVERAGE, PowerStatus.OFF, WorkingStatus.ALERT);

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "workingStatus",
        "validation.constraints.vending_machine.working_status_consistency");
  }

  @Test
  void powerOnMachineMustHaveWorkingStatus()
  {
    dto = buildDto("123456789", "123 rue Sésame", ItemType.COLD_BAVERAGE, PowerStatus.ON, null);

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "workingStatus", "validation.constraints.empty_value");
  }
}
