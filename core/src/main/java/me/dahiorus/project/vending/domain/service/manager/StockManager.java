package me.dahiorus.project.vending.domain.service.manager;

import java.util.UUID;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.VendingMachine;

public interface StockManager
{
  VendingMachine getMachine(UUID id) throws EntityNotFound;

  void provision(VendingMachine vendingMachine, Item item, int quantity);
}
