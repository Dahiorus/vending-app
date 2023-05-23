package me.dahiorus.project.vending.domain.service.manager;

import java.util.UUID;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.VendingMachineNotWorking;
import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.Sale;
import me.dahiorus.project.vending.domain.model.VendingMachine;

public interface SaleManager
{
  VendingMachine getWorkingMachine(UUID id) throws EntityNotFound, VendingMachineNotWorking;

  Sale purchaseItem(VendingMachine vendingMachine, Item itemToPurchase);
}
