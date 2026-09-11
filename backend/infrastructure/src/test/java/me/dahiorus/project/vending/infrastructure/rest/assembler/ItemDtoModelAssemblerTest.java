package me.dahiorus.project.vending.infrastructure.rest.assembler;

import static java.util.stream.StreamSupport.stream;
import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.ITEM_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.hateoas.IanaLinkRelations.SELF;
import static org.springframework.http.HttpMethod.POST;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import me.dahiorus.project.vending.domain.item.entity.ItemType;
import me.dahiorus.project.vending.infrastructure.rest.entity.item.ItemDto;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

class ItemDtoModelAssemblerTest {
  private final ItemDtoModelAssembler assembler = new ItemDtoModelAssembler();

  @Test
  void should_add_self_link() {
    // Given
    var id = UUID.randomUUID();
    var item = new ItemDto(id, "Water", ItemType.SNACK, BigDecimal.ONE);
    var resource = EntityModel.of(item);

    // When
    assembler.addLinks(resource);

    // Then
    assertThat(resource.getRequiredLink(SELF).getHref()).endsWith("/api/v1/items/" + id);
  }

  @Test
  void should_add_item_image_link_with_upload_affordance() {
    // Given
    var id = UUID.randomUUID();
    var item = new ItemDto(id, "Water", ItemType.SNACK, BigDecimal.ONE);
    var resource = EntityModel.of(item);

    // When
    assembler.addLinks(resource);

    // Then
    var imageLink = resource.getRequiredLink(ITEM_IMAGE);
    assertThat(imageLink.getHref()).endsWith("/api/v1/items/" + id + "/image");

    var affordanceMethods =
        imageLink.getAffordances().stream()
            .flatMap(affordance -> stream(affordance.spliterator(), false))
            .map(model -> model.getName() + ":" + model.getHttpMethod())
            .toList();
    assertThat(affordanceMethods).contains("uploadImage:" + POST);
  }

  @Test
  void should_do_nothing_on_collection_model() {
    // Given
    var resources = CollectionModel.of(List.<EntityModel<ItemDto>>of());

    // When
    assembler.addLinks(resources);

    // Then
    assertThat(resources.getLinks()).isEmpty();
  }
}
