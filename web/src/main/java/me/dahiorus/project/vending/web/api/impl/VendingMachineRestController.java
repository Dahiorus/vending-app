package me.dahiorus.project.vending.web.api.impl;

import static org.springframework.http.ResponseEntity.ok;

import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.model.dto.VendingMachineDto;
import me.dahiorus.project.vending.domain.service.VendingMachineDtoService;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "VendingMachine", description = "Operations on Vending machine")
@RestController
@RequestMapping(value = "/api/v1/vending-machines")
@Log4j2
public class VendingMachineRestController extends RestControllerImpl<VendingMachineDto, VendingMachineDtoService>
{
  public VendingMachineRestController(final VendingMachineDtoService dtoService,
    final RepresentationModelAssembler<VendingMachineDto, EntityModel<VendingMachineDto>> modelAssembler,
    final PagedResourcesAssembler<VendingMachineDto> pageModelAssembler)
  {
    super(dtoService, modelAssembler, pageModelAssembler);
  }

  @Override
  public Logger getLogger()
  {
    return log;
  }

  @Operation(description = "Reset all error statuses of a vending machine")
  @ApiResponse(responseCode = "200", description = "Error statuses reset")
  @PostMapping(value = "/{id}/reset", produces = MediaTypes.HAL_JSON_VALUE)
  public ResponseEntity<EntityModel<VendingMachineDto>> resetStatus(@PathVariable("id") final UUID id)
    throws EntityNotFound
  {
    VendingMachineDto updatedEntity = dtoService.resetStatus(id);

    log.info("Status of the vending machine has been reset: {}", updatedEntity);

    return ok(modelAssembler.toModel(updatedEntity));
  }
}
