package me.dahiorus.project.vending.infrastructure.rest.assembler;

import static java.util.stream.StreamSupport.stream;
import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.ITEM;
import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.VENDING_MACHINE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;
import java.util.UUID;
import me.dahiorus.project.vending.infrastructure.rest.entity.stock.VendingMachineStockDto;
import me.dahiorus.project.vending.infrastructure.rest.entity.stock.VendingMachineStockDto.ItemQuantityDto;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

class VendingMachineStockDtoModelAssemblerTest {
  private final VendingMachineStockDtoModelAssembler assembler =
      new VendingMachineStockDtoModelAssembler();

  @Test
  void should_add_self_and_vending_machine_and_provision_links() {
    // Given
    var vendingMachineId = UUID.randomUUID();
    var stock = new VendingMachineStockDto(vendingMachineId, new LinkedHashSet<>());
    var resource = EntityModel.of(stock);

    // When
    assembler.addLinks(resource);

    // Then
    assertThat(resource.getRequiredLink("self").getHref())
        .endsWith("/api/v1/vending-machines/" + vendingMachineId + "/stock");
    assertThat(resource.getRequiredLink(VENDING_MACHINE).getHref())
        .endsWith("/api/v1/vending-machines/" + vendingMachineId);
    var provisionLink = resource.getRequiredLink("stock:provision");
    assertThat(provisionLink.getHref())
        .endsWith("/api/v1/vending-machines/" + vendingMachineId + "/stock");
    assertThat(affordanceMethodsOf(provisionLink)).contains("provisionStock:" + POST);
  }

  @Test
  void should_add_item_and_order_links_for_each_item_quantity() {
    // Given
    var vendingMachineId = UUID.randomUUID();
    var firstItemId = UUID.randomUUID();
    var secondItemId = UUID.randomUUID();
    SequencedSet<ItemQuantityDto> itemQuantities = new LinkedHashSet<>();
    itemQuantities.add(new ItemQuantityDto(firstItemId, "Coca-Cola", 5));
    itemQuantities.add(new ItemQuantityDto(secondItemId, "Water", 3));
    var stock = new VendingMachineStockDto(vendingMachineId, itemQuantities);
    var resource = EntityModel.of(stock);

    // When
    assembler.addLinks(resource);

    // Then
    assertThat(resource.getLinks(ITEM))
        .extracting(Link::getHref)
        .containsExactlyInAnyOrder(
            "/api/v1/items/" + firstItemId, "/api/v1/items/" + secondItemId);
    assertThat(resource.getLinks("order"))
        .extracting(Link::getHref)
        .containsExactlyInAnyOrder(
            "/api/v1/vending-machines/" + vendingMachineId + "/order/" + firstItemId,
            "/api/v1/vending-machines/" + vendingMachineId + "/order/" + secondItemId);
    resource
        .getLinks("order")
        .forEach(
            orderLink ->
                assertThat(affordanceMethodsOf(orderLink)).contains("orderItem:" + POST));
  }

  @Test
  void should_do_nothing_on_collection_model() {
    // Given
    var resources = CollectionModel.of(List.<EntityModel<VendingMachineStockDto>>of());

    // When
    assembler.addLinks(resources);

    // Then
    assertThat(resources.getLinks()).isEmpty();
  }

  private static List<String> affordanceMethodsOf(Link link) {
    return link.getAffordances().stream()
        .flatMap(affordance -> stream(affordance.spliterator(), false))
        .map(model -> model.getName() + ":" + model.getHttpMethod())
        .toList();
  }
}
