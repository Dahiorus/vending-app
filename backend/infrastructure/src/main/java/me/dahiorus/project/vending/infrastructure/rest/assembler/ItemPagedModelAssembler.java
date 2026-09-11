package me.dahiorus.project.vending.infrastructure.rest.assembler;

import static org.springframework.hateoas.IanaLinkRelations.SELF;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import me.dahiorus.project.vending.infrastructure.rest.controller.item.ItemCrudRestController;
import me.dahiorus.project.vending.infrastructure.rest.entity.item.ItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class ItemPagedModelAssembler implements PagedModelAssembler<ItemDto> {
  private final PagedResourcesAssembler<ItemDto> pageAssembler;
  private final RepresentationModelAssembler<ItemDto, EntityModel<ItemDto>> entityModelAssembler;

  public ItemPagedModelAssembler(PagedResourcesAssembler<ItemDto> pageAssembler,
      RepresentationModelAssembler<ItemDto, EntityModel<ItemDto>> entityModelAssembler) {
    this.pageAssembler = pageAssembler;
    this.entityModelAssembler = entityModelAssembler;
  }

  @Override
  public PagedModel<EntityModel<ItemDto>> toModel(Page<ItemDto> page) {
    var pagedModel = pageAssembler.toModel(page, entityModelAssembler);

    pagedModel.mapLink(
        SELF,
        link ->
            link.andAffordance(
                afford(methodOn(ItemCrudRestController.class).create(null))));

    return pagedModel;
  }
}
