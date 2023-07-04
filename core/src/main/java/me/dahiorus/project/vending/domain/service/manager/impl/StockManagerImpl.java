package me.dahiorus.project.vending.domain.service.manager.impl;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.dao.DAO;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.Stock;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.service.manager.StockManager;

@Log4j2
@RequiredArgsConstructor
@Component
public class StockManagerImpl implements StockManager
{
  private final DAO<Stock> dao;

  private final DAO<VendingMachine> vendingMachineDao;

  @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
  @Override
  public VendingMachine getMachine(final UUID id) throws EntityNotFound
  {
    return vendingMachineDao.read(id);
  }

  @Transactional(propagation = Propagation.MANDATORY)
  @Override
  public void provision(final VendingMachine vendingMachine, final Item itemToProvision,
    final Integer quantity)
  {
    log.debug("Provisioning a quantity of {} of item '{}' to vending machine {}", quantity,
      itemToProvision.getName(), vendingMachine.getId());

    vendingMachine.findStock(itemToProvision)
      .ifPresentOrElse(s -> {
        s.addQuantity(quantity);
        dao.save(s);
        log.debug("Provisioned stock of items '{}': stock quantity updated to {}",
          itemToProvision.getName(), s.getQuantity());
      }, () -> {
        Stock stock = Stock.fill(itemToProvision, quantity);
        vendingMachine.addStock(stock);
        dao.save(stock);
        log.debug("Provisioned a new stock of items to the vending machine {}: '{}' with quantity of {}",
          vendingMachine.getId(), itemToProvision.getName(), quantity);
      });

    vendingMachine.markIntervention();
    vendingMachineDao.save(vendingMachine);
  }
}
