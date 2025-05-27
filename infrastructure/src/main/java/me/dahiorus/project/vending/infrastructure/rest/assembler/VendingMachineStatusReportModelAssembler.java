package me.dahiorus.project.vending.infrastructure.rest.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Optional;
import java.util.Set;
import me.dahiorus.project.vending.infrastructure.rest.controller.machine.VendingMachineCrudRestController;
import me.dahiorus.project.vending.infrastructure.rest.entity.machine.VendingMachineStatusReportDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class VendingMachineStatusReportModelAssembler
    implements SimpleRepresentationModelAssembler<VendingMachineStatusReportDto> {

  @Override
  public void addLinks(final EntityModel<VendingMachineStatusReportDto> resource) {
    Optional.ofNullable(resource.getContent())
        .map(VendingMachineStatusReportModelAssembler::buildLinks)
        .ifPresent(resource::add);
  }

  private static Set<Link> buildLinks(VendingMachineStatusReportDto content) {
    return Set.of(
        linkTo(methodOn(VendingMachineCrudRestController.class).read(content.vendingMachineId()))
            .withRel(Relation.VENDING_MACHINE));
  }

  @Override
  public void addLinks(
      final CollectionModel<EntityModel<VendingMachineStatusReportDto>> resources) {}
}
