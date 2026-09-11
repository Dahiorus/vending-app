package me.dahiorus.project.vending.infrastructure.rest.assembler;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.STOCK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.hateoas.IanaLinkRelations.SELF;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import me.dahiorus.project.vending.infrastructure.rest.entity.machine.VendingMachineDto;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpMethod;

class VendingMachineDtoModelAssemblerTest {
  private final VendingMachineDtoModelAssembler assembler = new VendingMachineDtoModelAssembler();

  @Test
  void should_add_self_link_with_update_and_delete_affordances() {
    // Given
    var id = UUID.randomUUID();
    var vendingMachine =
        new VendingMachineDto(id, "SN-001", null, null, null, null, null, null, null, null, null);
    var resource = EntityModel.of(vendingMachine);

    // When
    assembler.addLinks(resource);

    // Then
    var selfLink = resource.getRequiredLink(SELF);
    assertThat(selfLink.getHref()).endsWith("/api/v1/vending-machines/" + id);

    var affordanceMethods = affordanceNames(selfLink);
    assertThat(affordanceMethods)
        .containsExactlyInAnyOrder(
            "read:" + HttpMethod.GET, "update:" + HttpMethod.PUT, "delete:" + HttpMethod.DELETE);
  }

  @Test
  void should_add_status_reset_link_with_affordance() {
    // Given
    var id = UUID.randomUUID();
    var vendingMachine =
        new VendingMachineDto(id, "SN-001", null, null, null, null, null, null, null, null, null);
    var resource = EntityModel.of(vendingMachine);

    // When
    assembler.addLinks(resource);

    // Then
    var resetLink = resource.getRequiredLink("status:reset");
    assertThat(resetLink.getHref()).endsWith("/api/v1/vending-machines/" + id + "/reset");
    assertThat(affordanceNames(resetLink)).containsExactly("resetStatus:" + HttpMethod.POST);
  }

  @Test
  void should_add_status_report_link_with_affordance() {
    // Given
    var id = UUID.randomUUID();
    var vendingMachine =
        new VendingMachineDto(id, "SN-001", null, null, null, null, null, null, null, null, null);
    var resource = EntityModel.of(vendingMachine);

    // When
    assembler.addLinks(resource);

    // Then
    var reportLink = resource.getRequiredLink("status:report");
    assertThat(reportLink.getHref()).endsWith("/api/v1/vending-machines/" + id + "/status/report");
    assertThat(affordanceNames(reportLink)).containsExactly("reportStatus:" + HttpMethod.POST);
  }

  @Test
  void should_add_stock_relation_link() {
    // Given
    var id = UUID.randomUUID();
    var vendingMachine =
        new VendingMachineDto(id, "SN-001", null, null, null, null, null, null, null, null, null);
    var resource = EntityModel.of(vendingMachine);

    // When
    assembler.addLinks(resource);

    // Then
    assertThat(resource.getRequiredLink(STOCK).getHref())
        .endsWith("/api/v1/vending-machines/" + id + "/stock");
  }

  @Test
  void should_do_nothing_on_collection_model() {
    // Given
    var resources = CollectionModel.of(List.<EntityModel<VendingMachineDto>>of());

    // When
    assembler.addLinks(resources);

    // Then
    assertThat(resources.getLinks()).isEmpty();
  }

  private static Set<String> affordanceNames(Link link) {
    return link.getAffordances().stream()
        .flatMap(affordance -> stream(affordance.spliterator(), false))
        .map(model -> model.getName() + ":" + model.getHttpMethod())
        .collect(toSet());
  }
}
