package me.dahiorus.project.vending.domain.service;

import java.util.UUID;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ItemMissing;
import me.dahiorus.project.vending.domain.exception.VendingMachineNotWorking;
import me.dahiorus.project.vending.domain.model.dto.ItemDTO;
import me.dahiorus.project.vending.domain.model.dto.SaleDTO;
import me.dahiorus.project.vending.domain.model.dto.VendingMachineDTO;

public interface SaleDtoService
{
  /**
   * Purchase an item from a vending machine
   * @param id The ID of the {@link VendingMachineDTO vending machine}
   * @param item The {@link ItemDTO item} to purchase
   * @return A {@link SaleDTO sale} of the item
   * @throws EntityNotFound if no vending machine match the ID
   * @throws ItemMissing if the item to purchase is not in the machine
   * @throws VendingMachineNotWorking if the vending machine is not working
   */
  SaleDTO purchaseItem(UUID id, ItemDTO item)
    throws EntityNotFound, ItemMissing, VendingMachineNotWorking;
}
