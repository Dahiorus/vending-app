package me.dahiorus.project.vending.infrastructure.rest.assembler;

import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.VENDING_MACHINE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Optional;
import java.util.Set;
import me.dahiorus.project.vending.infrastructure.rest.controller.machine.VendingMachineCrudRestController;
import me.dahiorus.project.vending.infrastructure.rest.entity.machine.ClientOrderDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class ClientOrderDtoModelAssembler
    implements SimpleRepresentationModelAssembler<ClientOrderDto> {

  @Override
  public void addLinks(final EntityModel<ClientOrderDto> resource) {
    Optional.ofNullable(resource.getContent())
        .map(ClientOrderDtoModelAssembler::buildLinks)
        .ifPresent(resource::add);
  }

  private static Set<Link> buildLinks(ClientOrderDto content) {
    // TODO add link for item
    return Set.of(
        linkTo(methodOn(VendingMachineCrudRestController.class).read(content.vendingMachineId()))
            .withRel(VENDING_MACHINE));
  }

  @Override
  public void addLinks(final CollectionModel<EntityModel<ClientOrderDto>> resources) {}
}
