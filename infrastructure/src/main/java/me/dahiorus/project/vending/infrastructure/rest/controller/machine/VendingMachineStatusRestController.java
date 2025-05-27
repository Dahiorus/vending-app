package me.dahiorus.project.vending.infrastructure.rest.controller.machine;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.reporting.port.VendingMachineStatusApiPort;
import me.dahiorus.project.vending.infrastructure.rest.entity.machine.VendingMachineDto;
import me.dahiorus.project.vending.infrastructure.rest.entity.machine.VendingMachineStatusReportDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping(value = "/api/v1/vending-machines/{id}", produces = HAL_JSON_VALUE)
public class VendingMachineStatusRestController {

  private final VendingMachineStatusApiPort vendingMachineStatusService;
  private final RepresentationModelAssembler<VendingMachineDto, EntityModel<VendingMachineDto>>
      vendingMachineModelAssembler;
  private final RepresentationModelAssembler<
          VendingMachineStatusReportDto, EntityModel<VendingMachineStatusReportDto>>
      statusReportModelAssembler;

  public VendingMachineStatusRestController(
      VendingMachineStatusApiPort vendingMachineStatusService,
      RepresentationModelAssembler<VendingMachineDto, EntityModel<VendingMachineDto>>
          vendingMachineModelAssembler,
      RepresentationModelAssembler<
              VendingMachineStatusReportDto, EntityModel<VendingMachineStatusReportDto>>
          statusReportModelAssembler) {
    this.vendingMachineStatusService = vendingMachineStatusService;
    this.vendingMachineModelAssembler = vendingMachineModelAssembler;
    this.statusReportModelAssembler = statusReportModelAssembler;
  }

  @Operation(description = "Reset all error statuses of a vending machine")
  @Tag(name = "VendingMachine")
  @ApiResponse(responseCode = "200", description = "Error statuses reset")
  @PostMapping("/reset")
  public ResponseEntity<EntityModel<VendingMachineDto>> resetStatus(
      @PathVariable("id") final UUID vendingMachineId) {
    var repairedVendingMachine =
        vendingMachineStatusService.resetStatus(new VendingMachineId(vendingMachineId));
    var vendingMachineDto = VendingMachineDto.fromDomain(repairedVendingMachine);

    return ok(vendingMachineModelAssembler.toModel(vendingMachineDto));
  }

  @Tag(name = "Reporting")
  @Operation(description = "Generate a report of the current status of a vending machine")
  @ApiResponse(responseCode = "200", description = "Report generated")
  @PostMapping("/status/report")
  public ResponseEntity<EntityModel<VendingMachineStatusReportDto>> reportStatus(
      @PathVariable("id") final UUID id) {
    var vendingMachineId = new VendingMachineId(id);
    var statusReport = vendingMachineStatusService.reportStatus(vendingMachineId);
    var reportDto = VendingMachineStatusReportDto.fromDomain(vendingMachineId, statusReport);

    return ok().body(statusReportModelAssembler.toModel(reportDto));
  }
}
