package me.dahiorus.project.vending.infrastructure.rest.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Optional;
import java.util.Set;
import me.dahiorus.project.vending.infrastructure.rest.controller.machine.VendingMachineCrudRestController;
import me.dahiorus.project.vending.infrastructure.rest.controller.machine.VendingMachineStatusRestController;
import me.dahiorus.project.vending.infrastructure.rest.controller.machine.VendingMachineStockRestController;
import me.dahiorus.project.vending.infrastructure.rest.entity.machine.VendingMachineDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class VendingMachineDtoModelAssembler
    implements SimpleRepresentationModelAssembler<VendingMachineDto> {

  @Override
  public void addLinks(EntityModel<VendingMachineDto> resource) {
    Optional.ofNullable(resource.getContent())
        .map(VendingMachineDtoModelAssembler::linksOf)
        .ifPresent(resource::add);
  }

  private static Set<Link> linksOf(VendingMachineDto content) {
    return Set.of(
        linkTo(methodOn(VendingMachineCrudRestController.class).read(content.id()))
            .withSelfRel()
            .andAffordance(
                afford(methodOn(VendingMachineCrudRestController.class).update(content.id(), null)))
            .andAffordance(
                afford(methodOn(VendingMachineCrudRestController.class).delete(content.id()))),
        linkTo(methodOn(VendingMachineStatusRestController.class).resetStatus(content.id()))
            .withRel("status:reset")
            .andAffordance(
                afford(
                    methodOn(VendingMachineStatusRestController.class).resetStatus(content.id()))),
        linkTo(methodOn(VendingMachineStatusRestController.class).reportStatus(content.id()))
            .withRel("status:report")
            .andAffordance(
                afford(
                    methodOn(VendingMachineStatusRestController.class).reportStatus(content.id()))),
        linkTo(methodOn(VendingMachineStockRestController.class).getStock(content.id()))
            .withRel("stock"));
  }

  @Override
  public void addLinks(CollectionModel<EntityModel<VendingMachineDto>> resources) {
    // no action
  }
}
