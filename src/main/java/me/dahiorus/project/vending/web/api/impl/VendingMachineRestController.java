package me.dahiorus.project.vending.web.api.impl;

import static org.springframework.http.ResponseEntity.ok;

import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ItemMissing;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.core.model.dto.SaleDTO;
import me.dahiorus.project.vending.core.model.dto.StockDTO;
import me.dahiorus.project.vending.core.model.dto.VendingMachineDTO;
import me.dahiorus.project.vending.core.service.ItemDtoService;
import me.dahiorus.project.vending.core.service.VendingMachineDtoService;
import me.dahiorus.project.vending.web.api.model.ProvisionRequest;

@Tag(name = "VendingMachine", description = "Operations on Vending machine")
@RestController
@RequestMapping(value = "/api/v1/vending-machines")
@Log4j2
public class VendingMachineRestController extends RestControllerImpl<VendingMachineDTO, VendingMachineDtoService>
{
  private final ItemDtoService itemDtoService;

  private final RepresentationModelAssembler<StockDTO, EntityModel<StockDTO>> stockModelAssembler;

  public VendingMachineRestController(final VendingMachineDtoService dtoService,
      final RepresentationModelAssembler<VendingMachineDTO, EntityModel<VendingMachineDTO>> modelAssembler,
      final PagedResourcesAssembler<VendingMachineDTO> pageModelAssembler,
      final ItemDtoService itemDtoService,
      final RepresentationModelAssembler<StockDTO, EntityModel<StockDTO>> stockModelAssembler)
  {
    super(dtoService, modelAssembler, pageModelAssembler);
    this.itemDtoService = itemDtoService;
    this.stockModelAssembler = stockModelAssembler;
  }

  @Override
  public Logger getLogger()
  {
    return log;
  }

  @Operation(description = "Get the stocks of a vending machine")
  @GetMapping("/{id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}/stocks")
  public ResponseEntity<CollectionModel<EntityModel<StockDTO>>> getStocks(@PathVariable final UUID id)
      throws EntityNotFound
  {
    return ok(stockModelAssembler.toCollectionModel(dtoService.getStocks(id)));
  }

  @Operation(description = "Provision a stock of one item to a vending machine")
  @PostMapping("/{id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}/provision"
      + "/{itemId:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}")
  public ResponseEntity<CollectionModel<EntityModel<StockDTO>>> provisionStock(@PathVariable("id") final UUID id,
      @PathVariable("itemId") final UUID itemId,
      @RequestBody final ProvisionRequest provisionRequest)
      throws EntityNotFound, ValidationException
  {
    ItemDTO item = itemDtoService.read(itemId);
    dtoService.provisionStock(id, item, provisionRequest.getQuantity());

    log.info("Provisioned stock of {} for vending machine {} with {}", itemId, id, provisionRequest);

    return ok(stockModelAssembler.toCollectionModel(dtoService.getStocks(id)));
  }

  @Operation(description = "Purchase an item from a vending machine")
  @PostMapping("/{id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}/purchase"
      + "/{itemId:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}")
  public ResponseEntity<EntityModel<SaleDTO>> purchaseItem(@PathVariable("id") final UUID id,
      @PathVariable("itemId") final UUID itemId) throws EntityNotFound, ItemMissing
  {
    ItemDTO item = itemDtoService.read(itemId);
    SaleDTO sale = dtoService.purchaseItem(id, item);

    log.info("Purchased item {} from vending machine {}", itemId, id);

    return ok(EntityModel.of(sale));
  }
}
