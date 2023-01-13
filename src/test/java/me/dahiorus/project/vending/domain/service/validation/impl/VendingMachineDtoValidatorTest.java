package me.dahiorus.project.vending.domain.service.validation.impl;

import static me.dahiorus.project.vending.util.TestUtils.anySpec;
import static me.dahiorus.project.vending.util.TestUtils.assertHasExactlyFieldErrors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.dahiorus.project.vending.domain.dao.VendingMachineDAO;
import me.dahiorus.project.vending.domain.model.ItemType;
import me.dahiorus.project.vending.domain.model.PowerStatus;
import me.dahiorus.project.vending.domain.model.WorkingStatus;
import me.dahiorus.project.vending.domain.model.dto.VendingMachineDTO;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;
import me.dahiorus.project.vending.util.VendingMachineBuilder;

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
    when(dao.count(anySpec())).thenReturn(1L);

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "serialNumber", "validation.constraints.not_unique");
  }

  @Test
  void serialNumberHasMaxLength()
  {
    dto = buildDto(RandomStringUtils.randomAlphabetic(256), "1 Fake Street", ItemType.FOOD, PowerStatus.ON,
        WorkingStatus.OK);

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "serialNumber", "validation.constraints.max_length");
  }

  @ParameterizedTest(name = "Blank serial number [{0}] is not valid")
  @NullAndEmptySource
  @ValueSource(strings = "  ")
  void addressIsMandatory(final String address)
  {
    dto = buildDto("123456789", address, ItemType.FOOD, PowerStatus.ON, WorkingStatus.OK);

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "address.streetAddress", "validation.constraints.empty_value");
  }

  @Test
  void addressHasMaxLength()
  {
    dto = buildDto("1234", RandomStringUtils.randomAlphanumeric(256), ItemType.FOOD, PowerStatus.ON,
        WorkingStatus.OK);

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "address.streetAddress", "validation.constraints.max_length");
  }

  @Test
  void placeHasMaxLength()
  {
    dto = buildDto("1234", "1 Fake Street", ItemType.FOOD, PowerStatus.ON,
        WorkingStatus.OK);
    dto.getAddress()
      .setPlace(RandomStringUtils.randomAlphanumeric(256));

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "address.place", "validation.constraints.max_length");
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

  VendingMachineDTO buildDto(final String serialNumber, final String address, final ItemType type,
      final PowerStatus powerStatus, final WorkingStatus workingStatus)
  {
    return VendingMachineBuilder.builder()
      .serialNumber(serialNumber)
      .address(address)
      .itemType(type)
      .powerStatus(powerStatus)
      .workingStatus(workingStatus)
      .buildDto();
  }
}
