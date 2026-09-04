package me.dahiorus.project.vending.infrastructure.rest.entity.item;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import me.dahiorus.project.vending.domain.item.entity.Item;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.entity.ItemName;
import me.dahiorus.project.vending.domain.item.entity.ItemType;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "elements")
public record ItemDto(
    @Parameter(hidden = true) @Schema(accessMode = READ_ONLY) UUID id,
    String name,
    ItemType type,
    BigDecimal price) {
  public static ItemDto fromDomain(final Item item) {
    return new ItemDto(item.id().value(), item.name().value(), item.type(), item.price());
  }

  public Item toDomain() {
    return new Item(
        Optional.ofNullable(id).map(ItemId::new).orElse(null),
        Optional.ofNullable(name).map(ItemName::of).orElse(null),
        price,
        type);
  }
}
