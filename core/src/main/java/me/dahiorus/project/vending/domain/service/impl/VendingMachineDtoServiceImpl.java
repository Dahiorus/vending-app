package me.dahiorus.project.vending.domain.service.impl;

import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.dao.Dao;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.model.CardSystemStatus;
import me.dahiorus.project.vending.domain.model.ChangeSystemStatus;
import me.dahiorus.project.vending.domain.model.PowerStatus;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.WorkingStatus;
import me.dahiorus.project.vending.domain.model.dto.VendingMachineDto;
import me.dahiorus.project.vending.domain.service.DtoMapper;
import me.dahiorus.project.vending.domain.service.VendingMachineDtoService;
import me.dahiorus.project.vending.domain.service.validation.DtoValidator;

@Log4j2
@Service
public class VendingMachineDtoServiceImpl
  extends DtoServiceImpl<VendingMachine, VendingMachineDto, Dao<VendingMachine>>
  implements VendingMachineDtoService
{
  public VendingMachineDtoServiceImpl(final Dao<VendingMachine> dao, final DtoMapper dtoMapper,
    final DtoValidator<VendingMachineDto> dtoValidator)
  {
    super(dao, dtoMapper, dtoValidator);
  }

  @Override
  public Logger getLogger()
  {
    return log;
  }

  @Override
  protected Class<VendingMachineDto> getDomainClass()
  {
    return VendingMachineDto.class;
  }

  @Transactional
  @Override
  public VendingMachineDto resetStatus(final UUID id) throws EntityNotFound
  {
    VendingMachine entity = dao.read(id);

    if (entity.isAllSystemClear())
    {
      log.warn("Vending machine all system clear. Nothing to do");

      return dtoMapper.toDto(entity, getDomainClass());
    }

    log.debug("Reseting all status of the vending machine {}", id);

    entity.setPowerStatus(PowerStatus.ON);
    entity.setWorkingStatus(WorkingStatus.OK);
    entity.setRfidStatus(CardSystemStatus.NORMAL);
    entity.setSmartCardStatus(CardSystemStatus.NORMAL);
    entity.setChangeMoneyStatus(ChangeSystemStatus.NORMAL);
    entity.markIntervention();

    log.info("Vending machine {} all status have been reset", id);

    VendingMachine updatedMachine = dao.save(entity);

    return dtoMapper.toDto(updatedMachine, getDomainClass());
  }
}
