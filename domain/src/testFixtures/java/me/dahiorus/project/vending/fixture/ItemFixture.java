package me.dahiorus.project.vending.fixture;

import static me.dahiorus.project.vending.domain.item.entity.ItemType.COLD_BEVERAGE;
import static me.dahiorus.project.vending.domain.item.entity.ItemType.HOT_BEVERAGE;
import static me.dahiorus.project.vending.domain.item.entity.ItemType.SNACK;

import java.math.BigDecimal;
import java.util.UUID;
import me.dahiorus.project.vending.domain.item.entity.Item;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.entity.ItemName;
import me.dahiorus.project.vending.domain.item.entity.ItemType;

public class ItemFixture {
  public static Item aColdBeverage(String name, double price) {
    return anItem().name(name).price(price).type(COLD_BEVERAGE).build();
  }

  public static Item aHotBeverage(String name, double price) {
    return anItem().name(name).price(price).type(HOT_BEVERAGE).build();
  }

  public static Item aSnack(String name, double price) {
    return anItem().name(name).price(price).type(SNACK).build();
  }

  public static Builder anItem() {
    return new Builder()
        .id(new ItemId(UUID.randomUUID()))
        .name("Coca-Cola")
        .price(1.50)
        .type(COLD_BEVERAGE);
  }

  public static class Builder {
    private ItemId id;
    private String name;
    private ItemType type;
    private BigDecimal price;

    public Builder id(ItemId id) {
      this.id = id;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder type(ItemType type) {
      this.type = type;
      return this;
    }

    public Builder price(BigDecimal price) {
      this.price = price;
      return this;
    }

    public Builder price(double price) {
      return price(BigDecimal.valueOf(price));
    }

    public Item build() {
      return new Item(id, ItemName.of(name), price, type);
    }
  }
}
