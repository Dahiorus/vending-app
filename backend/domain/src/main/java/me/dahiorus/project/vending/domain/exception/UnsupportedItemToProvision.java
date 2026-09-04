package me.dahiorus.project.vending.domain.exception;

import static java.lang.String.format;

import me.dahiorus.project.vending.domain.item.entity.Item;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;

public class UnsupportedItemToProvision extends RuntimeException {
  public UnsupportedItemToProvision(Item item, VendingMachine machine) {
    super(
        format(
            "Cannot provision unsupported item '%s' in vending machine '%s'.",
            item.name().value(), machine.serialNumber().value()));
  }
}
