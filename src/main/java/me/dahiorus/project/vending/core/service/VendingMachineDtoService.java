package me.dahiorus.project.vending.core.service;

import java.util.List;
import java.util.UUID;

import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ItemMissing;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.model.dto.CommentDTO;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.core.model.dto.SaleDTO;
import me.dahiorus.project.vending.core.model.dto.StockDTO;
import me.dahiorus.project.vending.core.model.dto.VendingMachineDTO;

public interface VendingMachineDtoService extends DtoService<VendingMachine, VendingMachineDTO>
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
  void provisionStock(UUID id, ItemDTO item, Long quantity) throws EntityNotFound, ValidationException;

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

  /**
   * Get the comments of a vending machine
   *
   * @param id The ID of the {@link VendingMachineDTO vending machine}
   * @return The {@link CommentDTO comments} of the vending machine
   * @throws EntityNotFound if no vending machine match the ID
   */
  List<CommentDTO> getComments(UUID id) throws EntityNotFound;

  /**
   * Add a comment to a vending machine
   *
   * @param id      The ID of the {@link VendingMachineDTO vending machine}
   * @param comment The {@link CommentDTO comment} to add
   * @throws EntityNotFound      if no vending machine match the ID
   * @throws ValidationException if the comment contains invalid data
   */
  void comment(UUID id, CommentDTO comment) throws EntityNotFound, ValidationException;
}
