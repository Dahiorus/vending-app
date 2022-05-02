package me.dahiorus.project.vending.web.api.impl;

import org.apache.logging.log4j.Logger;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.model.dto.VendingMachineDTO;
import me.dahiorus.project.vending.core.service.VendingMachineDtoService;

@Tag(name = "VendingMachine", description = "Operations on Vending machine")
@RestController
@RequestMapping(value = "/api/v1/vending-machines")
@Log4j2
public class VendingMachineRestController extends RestControllerImpl<VendingMachineDTO, VendingMachineDtoService>
{
  public VendingMachineRestController(final VendingMachineDtoService dtoService,
      final RepresentationModelAssembler<VendingMachineDTO, EntityModel<VendingMachineDTO>> modelAssembler,
      final PagedResourcesAssembler<VendingMachineDTO> pageModelAssembler)
  {
    super(dtoService, modelAssembler, pageModelAssembler);
  }

  @Override
  public Logger getLogger()
  {
    return log;
  }
}
