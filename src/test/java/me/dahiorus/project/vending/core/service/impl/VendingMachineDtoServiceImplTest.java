package me.dahiorus.project.vending.core.service.impl;

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

import com.dahiorus.project.vending.util.VendingMachineBuilder;

import me.dahiorus.project.vending.core.dao.impl.VendingMachineDaoImpl;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.model.CardSystemStatus;
import me.dahiorus.project.vending.core.model.ChangeSystemStatus;
import me.dahiorus.project.vending.core.model.PowerStatus;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.model.WorkingStatus;
import me.dahiorus.project.vending.core.model.dto.VendingMachineDTO;
import me.dahiorus.project.vending.core.service.validation.impl.VendingMachineDtoValidator;

@ExtendWith(MockitoExtension.class)
class VendingMachineDtoServiceImplTest
{
  @Mock
  VendingMachineDaoImpl dao;

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
  class ResetStatusesTests
  {
    @Test
    void machineNotWorking() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(), PowerStatus.ON, WorkingStatus.ALERT,
          CardSystemStatus.ERROR, CardSystemStatus.NORMAL, ChangeSystemStatus.FULL);

      when(dao.read(machine.getId())).thenReturn(machine);
      when(dao.save(machine)).thenReturn(machine);

      VendingMachineDTO updatedMachine = dtoService.resetStatuses(machine.getId());

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

      VendingMachineDTO updatedMachine = dtoService.resetStatuses(machine.getId());

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

      assertThatExceptionOfType(EntityNotFound.class).isThrownBy(() -> dtoService.resetStatuses(id));
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
