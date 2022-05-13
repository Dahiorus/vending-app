package me.dahiorus.project.vending.web.api.assembler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.stream.Stream;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;

import me.dahiorus.project.vending.core.model.dto.AbstractDTO;

public final class Assertions
{
  public static <D extends AbstractDTO<?>> void assertEntityModel(final EntityModel<D> model, final D dto,
      final String... relations)
  {
    assertAll(() -> assertThat(model.getContent()).as("Model contains DTO")
      .isEqualTo(dto),
        () -> assertThat(model.getLink(IanaLinkRelations.SELF)).as("Model has self link")
          .isNotEmpty(),
        () -> assertRelationLinks(model, relations));

  }

  public static <D extends AbstractDTO<?>> void assertRelationLinks(final EntityModel<D> model,
      final String... relations)
  {
    Stream.of(relations)
      .forEach(rel -> assertThat(model.getLink(rel)).as("Relation '%s' is present in %s", rel, model.getLinks())
        .isNotEmpty());
  }
}
