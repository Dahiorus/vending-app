package me.dahiorus.project.vending.web.api.impl;

import static org.springframework.http.ResponseEntity.ok;

import java.util.UUID;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ItemMissing;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.core.model.dto.SaleDTO;
import me.dahiorus.project.vending.core.service.ItemDtoService;
import me.dahiorus.project.vending.core.service.SaleDtoService;
import me.dahiorus.project.vending.web.api.AppWebService;

@Log4j2
@AllArgsConstructor
@RestController
@Tag(name = "Sale", description = "Operations on the sales of a vending machine")
@RequestMapping(value = "/api/v1/vending-machines/{id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}")
public class SaleRestService implements AppWebService
{
  private final SaleDtoService dtoService;

  private final ItemDtoService itemDtoService;

  @Operation(description = "Purchase an item from a vending machine")
  @PostMapping("/purchase/{itemId:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}")
  public ResponseEntity<EntityModel<SaleDTO>> purchaseItem(@PathVariable("id") final UUID id,
      @PathVariable("itemId") final UUID itemId) throws EntityNotFound, ItemMissing
  {
    ItemDTO item = itemDtoService.read(itemId);
    SaleDTO sale = dtoService.purchaseItem(id, item);

    log.info("Purchased item {} from vending machine {}", itemId, id);

    return ok(EntityModel.of(sale));
  }
}
