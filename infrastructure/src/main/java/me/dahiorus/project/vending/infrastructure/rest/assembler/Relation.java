package me.dahiorus.project.vending.infrastructure.rest.assembler;

import org.springframework.hateoas.LinkRelation;

public enum Relation implements LinkRelation {
  VENDING_MACHINE("vendingMachine"),
  STOCK("stock"),
  RESET("reset"),
  ORDER("order"),
  ITEM("item"),
  ITEM_IMAGE("itemImage"),
  SELF_PICTURE("me:picture"),
  SELF_PASSWORD("me:password");

  private final String value;

  Relation(String value) {
    this.value = value;
  }

  @Override
  public String value() {
    return value;
  }
}
