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
  List<StockDTO> getStocks(UUID id) throws EntityNotFound;

  void provisionStock(UUID id, ItemDTO item, Long quantity) throws EntityNotFound, ValidationException;

  SaleDTO purchaseItem(UUID id, ItemDTO item) throws EntityNotFound, ItemMissing;

  List<CommentDTO> getComments(UUID id) throws EntityNotFound;

  void comment(UUID id, CommentDTO comment) throws EntityNotFound, ValidationException;
}
