package me.dahiorus.project.vending.infrastructure.rest.assembler;

import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.ITEM;
import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.VENDING_MACHINE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Optional;
import java.util.Set;
import me.dahiorus.project.vending.infrastructure.rest.controller.item.ItemCrudRestController;
import me.dahiorus.project.vending.infrastructure.rest.controller.machine.VendingMachineCrudRestController;
import me.dahiorus.project.vending.infrastructure.rest.controller.machine.VendingMachineStockRestController;
import me.dahiorus.project.vending.infrastructure.rest.entity.stock.StockEntryDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class VendingMachineStockDtoModelAssembler
    implements SimpleRepresentationModelAssembler<StockEntryDto> {

  @Override
  public void addLinks(final EntityModel<StockEntryDto> resource) {
    Optional.ofNullable(resource.getContent())
        .map(VendingMachineStockDtoModelAssembler::buildLinks)
        .ifPresent(resource::add);
  }

  private static Set<Link> buildLinks(StockEntryDto content) {
    return Set.of(
        linkTo(
                methodOn(VendingMachineStockRestController.class)
                    .getStock(content.vendingMachineId()))
            .withSelfRel(),
        linkTo(methodOn(VendingMachineCrudRestController.class).read(content.vendingMachineId()))
            .withRel(VENDING_MACHINE),
        linkTo(methodOn(ItemCrudRestController.class).read(content.itemId())).withRel(ITEM));
  }

  @Override
  public void addLinks(final CollectionModel<EntityModel<StockEntryDto>> resources) {}
}
