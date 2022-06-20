package me.dahiorus.project.vending.domain.service.impl;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.dao.DAO;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ItemMissing;
import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.Sale;
import me.dahiorus.project.vending.domain.model.Stock;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.dto.ItemDTO;
import me.dahiorus.project.vending.domain.model.dto.SaleDTO;
import me.dahiorus.project.vending.domain.service.DtoMapper;
import me.dahiorus.project.vending.domain.service.SaleDtoService;

@Log4j2
@RequiredArgsConstructor
@Service
public class SaleDtoServiceImpl implements SaleDtoService
{
  private final DAO<Sale> dao;

  private final DAO<VendingMachine> vendingMachineDao;

  private final DAO<Stock> stockDao;

  private final DtoMapper dtoMapper;

  @Transactional
  @Override
  public SaleDTO purchaseItem(final UUID id, final ItemDTO item)
    throws EntityNotFound, ItemMissing, VendingMachineNotWorking
  {
    log.traceEntry(() -> id, () -> item);

    VendingMachine machine = vendingMachineDao.read(id);
    Item itemToPurchase = dtoMapper.toEntity(item, Item.class);

    if (!machine.hasItem(itemToPurchase) || machine.getQuantityInStock(itemToPurchase) == 0L)
    {
      throw new ItemMissing("Vending machine " + id + " does not have item " + item.getName());
    }

    machine.getStock(itemToPurchase)
      .ifPresent(stock -> {
        log.debug("Purchasing '{}' from the vending machine {}", item.getName(), id);
        stock.decrementQuantity();
        stockDao.save(stock);
      });

    log.debug("Adding a new sale of '{}' with amount of {} to the vending machine {}",
      item.getName(), item.getPrice(), id);

    Sale sale = Sale.of(itemToPurchase, machine);
    sale = dao.save(sale);
    machine.addSale(sale);
    VendingMachine updatedMachine = vendingMachineDao.save(machine);

    log.info("Item '{}' purchased from vending machine {} for price {}", item.getName(),
      updatedMachine.getId(), sale.getAmount());

    return log.traceExit(dtoMapper.toDto(sale, SaleDTO.class));
  }
}
