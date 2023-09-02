package me.dahiorus.project.vending.domain.service.manager.impl;

import static me.dahiorus.project.vending.domain.model.Sale.sell;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.dao.Dao;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.VendingMachineNotWorking;
import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.Sale;
import me.dahiorus.project.vending.domain.model.Stock;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.service.manager.SaleManager;

@Log4j2
@RequiredArgsConstructor
@Component
public class SaleManagerImpl implements SaleManager
{
  private final Dao<Sale> dao;

  private final Dao<VendingMachine> vendingMachineDao;

  private final Dao<Stock> stockDao;

  @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
  @Override
  public VendingMachine getWorkingMachine(final UUID id) throws EntityNotFound, VendingMachineNotWorking
  {
    VendingMachine machine = vendingMachineDao.read(id);

    if (!machine.isWorking())
    {
      throw new VendingMachineNotWorking("The vending machine [" + id + "] is not working");
    }

    return machine;
  }

  @Transactional(propagation = Propagation.MANDATORY)
  @Override
  public Sale purchaseItem(final VendingMachine vendingMachine, final Item itemToPurchase)
  {
    vendingMachine.findStock(itemToPurchase)
      .ifPresent(stock -> {
        log.debug("Purchasing '{}' from the vending machine {}", itemToPurchase.getName(),
          vendingMachine.getId());
        stock.decrementQuantity();
        stockDao.save(stock);
      });

    log.debug("Adding a new sale of '{}' with amount of {} to the vending machine {}",
      itemToPurchase.getName(), itemToPurchase.getPrice(), vendingMachine.getId());

    Sale sale = sell(itemToPurchase, vendingMachine);
    sale = dao.save(sale);
    vendingMachine.addSale(sale);
    vendingMachineDao.save(vendingMachine);

    return sale;
  }
}
