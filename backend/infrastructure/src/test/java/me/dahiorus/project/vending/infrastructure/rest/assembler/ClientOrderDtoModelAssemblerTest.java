package me.dahiorus.project.vending.infrastructure.rest.assembler;

import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.ITEM;
import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.VENDING_MACHINE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import me.dahiorus.project.vending.infrastructure.rest.entity.machine.ClientOrderDto;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

class ClientOrderDtoModelAssemblerTest {
  private final ClientOrderDtoModelAssembler assembler = new ClientOrderDtoModelAssembler();

  @Test
  void should_add_vending_machine_link() {
    // Given
    var vendingMachineId = UUID.randomUUID();
    var itemId = UUID.randomUUID();
    var clientOrder =
        new ClientOrderDto(vendingMachineId, itemId, BigDecimal.TEN, LocalDateTime.now());
    var resource = EntityModel.of(clientOrder);

    // When
    assembler.addLinks(resource);

    // Then
    assertThat(resource.getRequiredLink(VENDING_MACHINE).getHref())
        .endsWith("/api/v1/vending-machines/" + vendingMachineId);
  }

  @Test
  void should_add_item_link() {
    // Given
    var vendingMachineId = UUID.randomUUID();
    var itemId = UUID.randomUUID();
    var clientOrder =
        new ClientOrderDto(vendingMachineId, itemId, BigDecimal.TEN, LocalDateTime.now());
    var resource = EntityModel.of(clientOrder);

    // When
    assembler.addLinks(resource);

    // Then
    assertThat(resource.getRequiredLink(ITEM).getHref()).endsWith("/api/v1/items/" + itemId);
  }

  @Test
  void should_do_nothing_on_collection_model() {
    // Given
    var resources = CollectionModel.of(List.<EntityModel<ClientOrderDto>>of());

    // When
    assembler.addLinks(resources);

    // Then
    assertThat(resources.getLinks()).isEmpty();
  }
}
