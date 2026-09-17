package me.dahiorus.project.vending.infrastructure.rest.controller;

import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.AUTHENTICATE;
import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.DOCS;
import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.ITEMS;
import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.ME;
import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.REGISTER;
import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.VENDING_MACHINES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.hateoas.IanaLinkRelations.SELF;

import org.junit.jupiter.api.Test;

class RootRestControllerTest {
  private final RootRestController controller = new RootRestController();

  @Test
  void should_expose_links_to_every_top_level_resource() {
    // When
    var index = controller.index();

    // Then
    assertThat(index.getRequiredLink(SELF).getHref()).endsWith("/api/v1");
    assertThat(index.getRequiredLink(ITEMS).getHref()).endsWith("/api/v1/items");
    assertThat(index.getRequiredLink(VENDING_MACHINES).getHref())
        .endsWith("/api/v1/vending-machines");
    assertThat(index.getRequiredLink(ME).getHref()).endsWith("/api/v1/me");
    assertThat(index.getRequiredLink(AUTHENTICATE).getHref()).endsWith("/api/v1/authenticate");
    assertThat(index.getRequiredLink(REGISTER).getHref()).endsWith("/api/v1/register");
    assertThat(index.getRequiredLink(DOCS).getHref()).endsWith("/v3/api-docs");
  }
}
