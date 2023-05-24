package me.dahiorus.project.vending.domain.service;

import java.util.List;
import java.util.UUID;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.dto.ItemDTO;
import me.dahiorus.project.vending.domain.model.dto.StockDTO;
import me.dahiorus.project.vending.domain.model.dto.VendingMachineDTO;

public interface StockDtoService
{
  /**
   * Get the stocks of a vending machine stocks
   *
   * @param id The ID of the {@link VendingMachineDTO vending machine}
   * @return The {@link StockDTO stocks} of the vending machine
   * @throws EntityNotFound if no vending machine match the ID
   */
  List<StockDTO> getStocks(UUID id) throws EntityNotFound;

  /**
   * Add a stock of item to a vending machine
   *
   * @param id       The ID of the {@link VendingMachineDTO vending machine}
   * @param item     The {@link ItemDTO item} to stock
   * @param quantity The quantity of items to stock in the machine
   * @throws EntityNotFound      if no vending machine match the ID
   * @throws ValidationException if the item cannot be stock in the machine
   */
  void provisionStock(UUID id, ItemDTO item, Integer quantity) throws EntityNotFound, ValidationException;
}
