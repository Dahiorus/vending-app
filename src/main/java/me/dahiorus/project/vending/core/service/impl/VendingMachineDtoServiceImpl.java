package me.dahiorus.project.vending.core.service.impl;

import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.dao.VendingMachineDAO;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.model.dto.CommentDTO;
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
      final DtoValidator<VendingMachineDTO> dtoValidator,
      final DtoValidator<CommentDTO> commentDtoValidator)
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
}
