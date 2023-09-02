package me.dahiorus.project.vending.domain.service;

import java.util.UUID;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ItemMissing;
import me.dahiorus.project.vending.domain.exception.VendingMachineNotWorking;
import me.dahiorus.project.vending.domain.model.dto.ItemDto;
import me.dahiorus.project.vending.domain.model.dto.SaleDto;
import me.dahiorus.project.vending.domain.model.dto.VendingMachineDto;

public interface SaleDtoService
{
  /**
   * Purchase an item from a vending machine
   * 
   * @param id   The ID of the {@link VendingMachineDto vending machine}
   * @param item The {@link ItemDto item} to purchase
   * @return A {@link SaleDto sale} of the item
   * @throws EntityNotFound           if no vending machine match the ID
   * @throws ItemMissing              if the item to purchase is not in the machine
   * @throws VendingMachineNotWorking if the vending machine is not working
   */
  SaleDto purchaseItem(UUID id, ItemDto item)
    throws EntityNotFound, ItemMissing, VendingMachineNotWorking;
}
