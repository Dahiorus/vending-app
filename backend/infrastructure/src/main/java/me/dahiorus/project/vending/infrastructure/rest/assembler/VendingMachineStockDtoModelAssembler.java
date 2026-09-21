package me.dahiorus.project.vending.infrastructure.rest.assembler;

import static java.util.stream.Collectors.toSet;
import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.ITEM;
import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.VENDING_MACHINE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import me.dahiorus.project.vending.infrastructure.rest.controller.item.ItemCrudRestController;
import me.dahiorus.project.vending.infrastructure.rest.controller.machine.VendingMachineCrudRestController;
import me.dahiorus.project.vending.infrastructure.rest.controller.machine.VendingMachineOrderRestController;
import me.dahiorus.project.vending.infrastructure.rest.controller.machine.VendingMachineStockRestController;
import me.dahiorus.project.vending.infrastructure.rest.entity.stock.VendingMachineStockDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class VendingMachineStockDtoModelAssembler
    implements SimpleRepresentationModelAssembler<VendingMachineStockDto> {

  @Override
  public void addLinks(final EntityModel<VendingMachineStockDto> resource) {
    Optional.ofNullable(resource.getContent())
        .map(VendingMachineStockDtoModelAssembler::linksOf)
        .ifPresent(resource::add);
  }

  private static Set<Link> linksOf(VendingMachineStockDto content) {
    var streamItemLinks =
        content.itemQuantities().stream()
            .map(itemQuantity -> linkOfItem(content.vendingMachineId(), itemQuantity.itemId()))
            .flatMap(Stream::distinct);

    return Stream.concat(
            streamItemLinks,
            Stream.of(
                linkTo(
                        methodOn(VendingMachineStockRestController.class)
                            .getStock(content.vendingMachineId()))
                    .withSelfRel(),
                linkTo(
                        methodOn(VendingMachineCrudRestController.class)
                            .read(content.vendingMachineId()))
                    .withRel(VENDING_MACHINE),
                linkTo(
                        methodOn(VendingMachineStockRestController.class)
                            .provisionStock(content.vendingMachineId(), null))
                    .withRel("stock:provision")
                    .andAffordance(
                        afford(
                            methodOn(VendingMachineStockRestController.class)
                                .provisionStock(content.vendingMachineId(), null))),
                linkTo(
                        methodOn(VendingMachineStockRestController.class)
                            .reportStock(content.vendingMachineId()))
                    .withRel("stock:report")
                    .andAffordance(
                        afford(
                            methodOn(VendingMachineStockRestController.class)
                                .reportStock(content.vendingMachineId())))))
        .collect(toSet());
  }

  private static Stream<Link> linkOfItem(UUID vendingMachineId, UUID itemId) {
    return Stream.of(
        linkTo(methodOn(ItemCrudRestController.class).read(itemId)).withRel(ITEM),
        linkTo(
                methodOn(VendingMachineOrderRestController.class)
                    .orderItem(vendingMachineId, itemId))
            .withRel("order")
            .andAffordance(
                afford(
                    methodOn(VendingMachineOrderRestController.class)
                        .orderItem(vendingMachineId, itemId))));
  }

  @Override
  public void addLinks(CollectionModel<EntityModel<VendingMachineStockDto>> resources) {
    // no action
  }
}
