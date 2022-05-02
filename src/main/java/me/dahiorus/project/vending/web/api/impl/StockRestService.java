package me.dahiorus.project.vending.web.api.impl;

import static org.springframework.http.ResponseEntity.ok;

import java.util.UUID;

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
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.core.model.dto.StockDTO;
import me.dahiorus.project.vending.core.service.ItemDtoService;
import me.dahiorus.project.vending.core.service.StockDtoService;
import me.dahiorus.project.vending.web.api.AppWebService;
import me.dahiorus.project.vending.web.api.model.ProvisionRequest;

@Log4j2
@AllArgsConstructor
@RestController
@Tag(name = "Stock", description = "Operations on the stocks of a vending machine")
@RequestMapping(value = "/api/v1/vending-machines/{id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}")
public class StockRestService implements AppWebService
{
  private final StockDtoService dtoService;

  private final ItemDtoService itemDtoService;

  private final RepresentationModelAssembler<StockDTO, EntityModel<StockDTO>> stockModelAssembler;

  @Operation(description = "Get the stocks of a vending machine")
  @GetMapping("/stocks")
  public ResponseEntity<CollectionModel<EntityModel<StockDTO>>> getStocks(@PathVariable final UUID id)
      throws EntityNotFound
  {
    return ok(stockModelAssembler.toCollectionModel(dtoService.getStocks(id)));
  }

  @Operation(description = "Provision a stock of one item to a vending machine")
  @PostMapping("/provision/{itemId:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}")
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
}
