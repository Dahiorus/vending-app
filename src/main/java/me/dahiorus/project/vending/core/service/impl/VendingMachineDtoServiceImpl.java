package me.dahiorus.project.vending.core.service.impl;

import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.dao.VendingMachineDAO;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.model.CardSystemStatus;
import me.dahiorus.project.vending.core.model.ChangeSystemStatus;
import me.dahiorus.project.vending.core.model.PowerStatus;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.model.WorkingStatus;
import me.dahiorus.project.vending.core.model.dto.VendingMachineDTO;
import me.dahiorus.project.vending.core.service.DtoMapper;
import me.dahiorus.project.vending.core.service.VendingMachineDtoService;
import me.dahiorus.project.vending.core.service.validation.DtoValidator;

@Log4j2
@Service
public class VendingMachineDtoServiceImpl
    extends DtoServiceImpl<VendingMachine, VendingMachineDTO, VendingMachineDAO>
    implements VendingMachineDtoService
{
  public VendingMachineDtoServiceImpl(final VendingMachineDAO dao, final DtoMapper dtoMapper,
      final DtoValidator<VendingMachineDTO> dtoValidator)
  {
    super(dao, dtoMapper, dtoValidator);
  }

  @Override
  public Logger getLogger()
  {
    return log;
  }

  @Override
  protected Class<VendingMachineDTO> getDomainClass()
  {
    return VendingMachineDTO.class;
  }

  @Transactional(rollbackFor = EntityNotFound.class)
  @Override
  public VendingMachineDTO resetStatuses(final UUID id) throws EntityNotFound
  {
    VendingMachine entity = dao.read(id);

    if (entity.isAllSystemClear())
    {
      log.warn("Vending machine all system clear. Nothing to do");

      return dtoMapper.toDto(entity, getDomainClass());
    }

    log.debug("Reseting all statuses of the vending machine {}", id);

    entity.setPowerStatus(PowerStatus.ON);
    entity.setWorkingStatus(WorkingStatus.OK);
    entity.setRfidStatus(CardSystemStatus.NORMAL);
    entity.setSmartCardStatus(CardSystemStatus.NORMAL);
    entity.setChangeMoneyStatus(ChangeSystemStatus.NORMAL);
    entity.markIntervention();

    log.info("Vending machine {} all statuses are reset", id);

    VendingMachine updatedMachine = dao.save(entity);

    return dtoMapper.toDto(updatedMachine, getDomainClass());
  }
}
