package me.dahiorus.project.vending.web.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import me.dahiorus.project.vending.domain.exception.AppException;
import me.dahiorus.project.vending.domain.model.dto.StockDto;
import me.dahiorus.project.vending.web.api.impl.ItemRestController;
import me.dahiorus.project.vending.web.api.impl.SaleRestController;
import me.dahiorus.project.vending.web.api.impl.StockRestController;
import me.dahiorus.project.vending.web.api.impl.VendingMachineRestController;

@Component
public class StockDtoModelAssembler extends DtoModelAssembler<StockDto>
{
  private static final Logger logger = LogManager.getLogger(StockDtoModelAssembler.class);

  @Override
  public Logger getLogger()
  {
    return logger;
  }

  @Override
  protected Iterable<Link> buildLinks(final StockDto content) throws AppException
  {
    UUID vendingMachineId = content.getVendingMachineId();
    Link machineLink = linkTo(methodOn(VendingMachineRestController.class).read(vendingMachineId)).withRel("machine");

    UUID itemId = content.getItemId();
    Link itemLink = linkTo(
      methodOn(ItemRestController.class).read(itemId))
        .withRel("item");
    Link provisionLink = linkTo(
      methodOn(StockRestController.class).provisionStock(vendingMachineId, itemId, null))
        .withRel("provision");
    Link purchaseLink = linkTo(
      methodOn(SaleRestController.class).purchaseItem(vendingMachineId, itemId))
        .withRel("purchase");

    return List.of(machineLink, itemLink, provisionLink, purchaseLink);
  }
}
