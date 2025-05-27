package me.dahiorus.project.vending.domain.exception;

import static java.lang.String.format;

import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;

public class NotWorkingVendingMachine extends RuntimeException {
  public NotWorkingVendingMachine(VendingMachine vendingMachine) {
    super(format("The vending machine %s is not working", vendingMachine.id()));
  }
}
