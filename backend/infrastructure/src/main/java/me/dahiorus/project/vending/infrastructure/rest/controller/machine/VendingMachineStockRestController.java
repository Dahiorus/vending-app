package me.dahiorus.project.vending.infrastructure.rest.controller.machine;

import static me.dahiorus.project.vending.infrastructure.rest.entity.stock.VendingMachineStockDto.fromDomain;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.reporting.port.VendingMachineStockReportApiPort;
import me.dahiorus.project.vending.domain.stock.port.VendingMachineStockApiPort;
import me.dahiorus.project.vending.infrastructure.rest.entity.stock.ItemToProvisionDto;
import me.dahiorus.project.vending.infrastructure.rest.entity.stock.VendingMachineStockDto;
import me.dahiorus.project.vending.infrastructure.rest.entity.stock.VendingMachineStockReportDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = "/api/v1/vending-machines/{id}/stock", produces = HAL_JSON_VALUE)
@RestController
public class VendingMachineStockRestController {
  private final VendingMachineStockApiPort vendingMachineStockService;
  private final RepresentationModelAssembler<VendingMachineStockDto, EntityModel<VendingMachineStockDto>>
      modelAssembler;
  private final VendingMachineStockReportApiPort vendingMachineStockReportService;

  public VendingMachineStockRestController(
      final VendingMachineStockApiPort vendingMachineStockService,
      final VendingMachineStockReportApiPort vendingMachineStockReportService,
      final RepresentationModelAssembler<VendingMachineStockDto, EntityModel<VendingMachineStockDto>>
          modelAssembler) {
    this.vendingMachineStockService = vendingMachineStockService;
    this.vendingMachineStockReportService = vendingMachineStockReportService;
    this.modelAssembler = modelAssembler;
  }

  @Tag(name = "VendingMachine")
  @Operation(description = "Provision stocks of one item to a vending machine")
  @ApiResponse(responseCode = "200", description = "Stock provisioned")
  @PostMapping
  public ResponseEntity<EntityModel<VendingMachineStockDto>> provisionStock(
      @PathVariable("id") final UUID id, @RequestBody final ItemToProvisionDto itemToProvision) {
    var vendingMachineId = new VendingMachineId(id);
    var provisionedStocks =
        vendingMachineStockService.provision(
            vendingMachineId, itemToProvision.toItemId(), itemToProvision.toQuantity());

    return ok(modelAssembler.toModel(fromDomain(vendingMachineId, provisionedStocks)));
  }

  @Tag(name = "VendingMachine")
  @Operation(description = "Get the stocks of a vending machine")
  @ApiResponse(responseCode = "200", description = "Stock found")
  @GetMapping
  public ResponseEntity<EntityModel<VendingMachineStockDto>> getStock(
      @PathVariable("id") UUID id) {
    var vendingMachineId = new VendingMachineId(id);
    var stocks = vendingMachineStockService.get(vendingMachineId);

    return ok(modelAssembler.toModel(fromDomain(vendingMachineId, stocks)));
  }

  @Tag(name = "Reporting")
  @Operation(description = "Generate a report of the current stock of a vending machine")
  @ApiResponse(responseCode = "200", description = "Report generated")
  @PostMapping("/report")
  public ResponseEntity<VendingMachineStockReportDto> reportStock(@PathVariable("id") UUID id) {
    var report = vendingMachineStockReportService.reportStock(new VendingMachineId(id));

    return ok(VendingMachineStockReportDto.fromDomain(report));
  }
}
