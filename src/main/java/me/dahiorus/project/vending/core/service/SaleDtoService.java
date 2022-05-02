package me.dahiorus.project.vending.core.service;

import java.util.UUID;

import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ItemMissing;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.core.model.dto.SaleDTO;
import me.dahiorus.project.vending.core.model.dto.VendingMachineDTO;

public interface SaleDtoService
{
  /**
   * Purchase an item from a vending machine
   *
   * @param id   The ID of the {@link VendingMachineDTO vending machine}
   * @param item The {@link ItemDTO item} to purchase
   * @return A {@link SaleDTO sale} of the item
   * @throws EntityNotFound if no vending machine match the ID
   * @throws ItemMissing    if the item to purchase is not in the machine
   */
  SaleDTO purchaseItem(UUID id, ItemDTO item) throws EntityNotFound, ItemMissing;
}
