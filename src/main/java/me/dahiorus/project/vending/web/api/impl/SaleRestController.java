package me.dahiorus.project.vending.web.api.impl;

import static org.springframework.http.ResponseEntity.ok;

import java.util.UUID;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ItemMissing;
import me.dahiorus.project.vending.domain.model.dto.ItemDTO;
import me.dahiorus.project.vending.domain.model.dto.SaleDTO;
import me.dahiorus.project.vending.domain.service.ItemDtoService;
import me.dahiorus.project.vending.domain.service.SaleDtoService;
import me.dahiorus.project.vending.web.api.AppWebService;

@Log4j2
@RequiredArgsConstructor
@RestController
@Tag(name = "Sale", description = "Operations on the sales of a vending machine")
@RequestMapping(value = "/api/v1/vending-machines/{id}", produces = MediaTypes.HAL_JSON_VALUE)
public class SaleRestController implements AppWebService
{
  private final SaleDtoService dtoService;

  private final ItemDtoService itemDtoService;

  @Operation(description = "Purchase an item from a vending machine")
  @ApiResponse(responseCode = "200", description = "Item purchased")
  @PostMapping("/purchase/{itemId}")
  public ResponseEntity<EntityModel<SaleDTO>> purchaseItem(@PathVariable("id") final UUID id,
      @PathVariable("itemId") final UUID itemId) throws EntityNotFound, ItemMissing
  {
    ItemDTO item = itemDtoService.read(itemId);
    SaleDTO sale = dtoService.purchaseItem(id, item);

    log.info("Purchased item {} from vending machine {}", itemId, id);

    return ok(EntityModel.of(sale));
  }
}
