package me.dahiorus.project.vending.core.service.impl;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.dao.AbstractDAO;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ItemMissing;
import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.Sale;
import me.dahiorus.project.vending.core.model.Stock;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.core.model.dto.SaleDTO;
import me.dahiorus.project.vending.core.service.DtoMapper;
import me.dahiorus.project.vending.core.service.SaleDtoService;

@Log4j2
@AllArgsConstructor
@Service
public class SaleDtoServiceImpl implements SaleDtoService
{
  private final AbstractDAO<Sale> dao;

  private final AbstractDAO<VendingMachine> vendingMachineDao;

  private final AbstractDAO<Stock> stockDao;

  private final DtoMapper dtoMapper;

  @Transactional(rollbackFor = { EntityNotFound.class, ItemMissing.class })
  @Override
  public SaleDTO purchaseItem(final UUID id, final ItemDTO item) throws EntityNotFound, ItemMissing
  {
    log.traceEntry(() -> id, () -> item);

    VendingMachine machine = vendingMachineDao.read(id);
    Item itemToPurchase = dtoMapper.toEntity(item, Item.class);

    if (!machine.hasItem(itemToPurchase) || machine.getQuantityInStock(itemToPurchase) == 0L)
    {
      throw new ItemMissing("Vending machine " + id + " does not have item " + item.getName());
    }

    machine.getStocks()
      .stream()
      .filter(stock -> Objects.equals(stock.getItem(), itemToPurchase))
      .findFirst()
      .ifPresent(stock -> {
        log.debug("Purchasing '{}' from the vending machine {}", item.getName(), id);
        stock.decrementQuantity();
        stockDao.save(stock);
      });

    log.debug("Adding a new sale of '{}' with amount of {} to the vending machine {}", item.getName(), item.getPrice(),
        id);

    Sale sale = Sale.of(itemToPurchase, machine);
    sale = dao.save(sale);
    machine.addSale(sale);
    VendingMachine updatedMachine = vendingMachineDao.save(machine);

    log.info("Item '{}' purchased from vending machine {} for price {}", item.getName(), updatedMachine.getId(),
        sale.getAmount());

    return log.traceExit(dtoMapper.toDto(sale, SaleDTO.class));
  }
}
