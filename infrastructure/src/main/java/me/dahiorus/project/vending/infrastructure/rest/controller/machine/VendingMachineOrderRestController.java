package me.dahiorus.project.vending.infrastructure.rest.controller.machine;

import static org.springframework.http.ResponseEntity.ok;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.port.OrderItemApiPort;
import me.dahiorus.project.vending.domain.reporting.port.VendingMachineClientOrdersReportApiPort;
import me.dahiorus.project.vending.infrastructure.rest.entity.machine.ClientOrderDto;
import me.dahiorus.project.vending.infrastructure.rest.entity.machine.VendingMachineClientOrdersReportDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = "/api/v1/vending-machines/{id}", produces = MediaTypes.HAL_JSON_VALUE)
@RestController
public class VendingMachineOrderRestController {
  private final OrderItemApiPort orderItemService;
  private final RepresentationModelAssembler<ClientOrderDto, EntityModel<ClientOrderDto>>
      orderModelAssembler;
  private final VendingMachineClientOrdersReportApiPort vendingMachineClientOrdersReportService;

  public VendingMachineOrderRestController(
      final OrderItemApiPort orderItemService,
      final VendingMachineClientOrdersReportApiPort vendingMachineClientOrdersReportService,
      final RepresentationModelAssembler<ClientOrderDto, EntityModel<ClientOrderDto>>
          orderModelAssembler) {
    this.orderItemService = orderItemService;
    this.vendingMachineClientOrdersReportService = vendingMachineClientOrdersReportService;
    this.orderModelAssembler = orderModelAssembler;
  }

  @Tag(name = "VendingMachine")
  @Operation(description = "Order an item from a vending machine")
  @ApiResponse(responseCode = "200", description = "Item ordered")
  @PostMapping("/order/{itemId}")
  public ResponseEntity<EntityModel<ClientOrderDto>> orderItem(
      @PathVariable("id") UUID vendingMachineId, @PathVariable("itemId") final UUID itemId) {
    var clientOrder =
        orderItemService.orderItem(new VendingMachineId(vendingMachineId), new ItemId(itemId));
    var clientOrderDto = ClientOrderDto.fromDomain(clientOrder);

    return ok(orderModelAssembler.toModel(clientOrderDto));
  }

  @Tag(name = "Reporting")
  @Operation(description = "Order an item from a vending machine")
  @ApiResponse(responseCode = "200", description = "Report generated")
  @PostMapping("/orders/report")
  public ResponseEntity<VendingMachineClientOrdersReportDto> reportClientOrders(
      @PathVariable("id") UUID vendingMachineId) {
    var vendingMachineClientOrdersReport =
        vendingMachineClientOrdersReportService.reportClientOrders(
            new VendingMachineId(vendingMachineId));
    var reportDto =
        VendingMachineClientOrdersReportDto.fromDomain(vendingMachineClientOrdersReport);

    return ok().body(reportDto);
  }
}
