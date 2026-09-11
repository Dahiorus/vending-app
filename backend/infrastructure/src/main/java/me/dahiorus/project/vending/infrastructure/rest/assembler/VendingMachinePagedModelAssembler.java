package me.dahiorus.project.vending.infrastructure.rest.assembler;

import static org.springframework.hateoas.IanaLinkRelations.SELF;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import me.dahiorus.project.vending.infrastructure.rest.controller.machine.VendingMachineCrudRestController;
import me.dahiorus.project.vending.infrastructure.rest.entity.machine.VendingMachineDto;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

/**
 * Assembles a paginated list of {@link VendingMachineDto} into a {@link PagedModel}, applying the
 * item-level links/affordances of {@link VendingMachineDtoModelAssembler} and adding a
 * {@code create} affordance on the collection's self link.
 */
@Component
public class VendingMachinePagedModelAssembler implements PagedModelAssembler<VendingMachineDto> {
  private final PagedResourcesAssembler<VendingMachineDto> pageAssembler;
  private final RepresentationModelAssembler<VendingMachineDto, EntityModel<VendingMachineDto>>
      entityModelAssembler;

  public VendingMachinePagedModelAssembler(
      final PagedResourcesAssembler<VendingMachineDto> pageAssembler,
      final RepresentationModelAssembler<VendingMachineDto, EntityModel<VendingMachineDto>>
          entityModelAssembler) {
    this.pageAssembler = pageAssembler;
    this.entityModelAssembler = entityModelAssembler;
  }

  @Override
  public PagedModel<EntityModel<VendingMachineDto>> toModel(final Page<VendingMachineDto> page) {
    var pagedModel = pageAssembler.toModel(page, entityModelAssembler);

    pagedModel.mapLink(
        SELF,
        link ->
            link.andAffordance(
                afford(methodOn(VendingMachineCrudRestController.class).create(null))));

    return pagedModel;
  }
}
