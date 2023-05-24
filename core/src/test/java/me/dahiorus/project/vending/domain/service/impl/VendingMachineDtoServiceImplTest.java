package me.dahiorus.project.vending.domain.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.dahiorus.project.vending.domain.dao.VendingMachineDAO;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.model.CardSystemStatus;
import me.dahiorus.project.vending.domain.model.ChangeSystemStatus;
import me.dahiorus.project.vending.domain.model.PowerStatus;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.WorkingStatus;
import me.dahiorus.project.vending.domain.model.dto.VendingMachineDTO;
import me.dahiorus.project.vending.domain.service.validation.impl.VendingMachineDtoValidator;
import me.dahiorus.project.vending.util.VendingMachineBuilder;

@ExtendWith(MockitoExtension.class)
class VendingMachineDtoServiceImplTest
{
  @Mock
  VendingMachineDAO dao;

  @Mock
  VendingMachineDtoValidator dtoValidator;

  VendingMachineDtoServiceImpl dtoService;

  @BeforeEach
  void setUp()
  {
    when(dao.getDomainClass()).thenReturn(VendingMachine.class);
    dtoService = new VendingMachineDtoServiceImpl(dao, new DtoMapperImpl(), dtoValidator);
  }

  @Nested
  class ResetStatusTests
  {
    @Test
    void machineNotWorking() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(), PowerStatus.ON, WorkingStatus.ALERT,
        CardSystemStatus.ERROR, CardSystemStatus.NORMAL, ChangeSystemStatus.FULL);

      when(dao.read(machine.getId())).thenReturn(machine);
      when(dao.save(machine)).thenReturn(machine);

      VendingMachineDTO updatedMachine = dtoService.resetStatus(machine.getId());

      assertThat(updatedMachine).hasFieldOrPropertyWithValue("powerStatus", PowerStatus.ON)
        .hasFieldOrPropertyWithValue("workingStatus", WorkingStatus.OK)
        .hasFieldOrPropertyWithValue("rfidStatus", CardSystemStatus.NORMAL)
        .hasFieldOrPropertyWithValue("smartCardStatus", CardSystemStatus.NORMAL)
        .hasFieldOrPropertyWithValue("changeMoneyStatus", ChangeSystemStatus.NORMAL);
      verify(dao).save(machine);
    }

    @Test
    void machineIsWorking() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(), PowerStatus.ON, WorkingStatus.OK,
        CardSystemStatus.NORMAL, CardSystemStatus.NORMAL, ChangeSystemStatus.NORMAL);

      when(dao.read(machine.getId())).thenReturn(machine);

      VendingMachineDTO updatedMachine = dtoService.resetStatus(machine.getId());

      assertThat(updatedMachine).hasFieldOrPropertyWithValue("powerStatus", PowerStatus.ON)
        .hasFieldOrPropertyWithValue("workingStatus", WorkingStatus.OK)
        .hasFieldOrPropertyWithValue("rfidStatus", CardSystemStatus.NORMAL)
        .hasFieldOrPropertyWithValue("smartCardStatus", CardSystemStatus.NORMAL)
        .hasFieldOrPropertyWithValue("changeMoneyStatus", ChangeSystemStatus.NORMAL);
      verify(dao, never()).save(any());
    }

    @Test
    void unexistingMachine() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(dao.read(id)).thenThrow(new EntityNotFound(VendingMachine.class, id));

      assertThatExceptionOfType(EntityNotFound.class).isThrownBy(() -> dtoService.resetStatus(id));
      verify(dao, never()).save(any());
    }
  }

  static VendingMachine buildMachine(final UUID id, final PowerStatus powerStatus, final WorkingStatus workingStatus,
    final CardSystemStatus rfidStatus, final CardSystemStatus smartCardStatus,
    final ChangeSystemStatus changeSystemStatus)
  {
    return VendingMachineBuilder.builder()
      .id(id)
      .powerStatus(powerStatus)
      .workingStatus(workingStatus)
      .rfidStatus(rfidStatus)
      .smartCardStatus(smartCardStatus)
      .changeMoneyStatus(changeSystemStatus)
      .build();
  }
}
