package me.dahiorus.project.vending.infrastructure.rest.assembler;

import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.ITEM_IMAGE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Optional;
import java.util.Set;
import me.dahiorus.project.vending.infrastructure.rest.controller.item.ItemCrudRestController;
import me.dahiorus.project.vending.infrastructure.rest.controller.item.ItemImageRestController;
import me.dahiorus.project.vending.infrastructure.rest.entity.item.ItemDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class ItemDtoModelAssembler implements SimpleRepresentationModelAssembler<ItemDto> {

  @Override
  public void addLinks(final EntityModel<ItemDto> resource) {
    Optional.ofNullable(resource.getContent())
        .map(ItemDtoModelAssembler::buildLinks)
        .ifPresent(resource::add);
  }

  private static Set<Link> buildLinks(ItemDto content) {
    return Set.of(
        linkTo(methodOn(ItemCrudRestController.class).read(content.id())).withSelfRel(),
        linkTo(methodOn(ItemImageRestController.class).getImage(content.id())).withRel(ITEM_IMAGE));
  }

  @Override
  public void addLinks(final CollectionModel<EntityModel<ItemDto>> resources) {
    // no action
  }
}
