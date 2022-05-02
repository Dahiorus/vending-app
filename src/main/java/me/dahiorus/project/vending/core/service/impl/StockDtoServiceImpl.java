package me.dahiorus.project.vending.core.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.dao.DAO;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.Stock;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.core.model.dto.StockDTO;
import me.dahiorus.project.vending.core.service.DtoMapper;
import me.dahiorus.project.vending.core.service.StockDtoService;
import me.dahiorus.project.vending.core.service.validation.StockValidator;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;

@Log4j2
@RequiredArgsConstructor
@Service
public class StockDtoServiceImpl implements StockDtoService
{
  private final DAO<Stock> dao;

  private final DAO<VendingMachine> vendingMachineDao;

  private final StockValidator stockValidator;

  private final DtoMapper dtoMapper;

  @Transactional(rollbackFor = EntityNotFound.class, readOnly = true)
  @Override
  public List<StockDTO> getStocks(final UUID id) throws EntityNotFound
  {
    VendingMachine entity = vendingMachineDao.read(id);
    List<StockDTO> stocks = entity.getStocks()
      .stream()
      .map(stock -> dtoMapper.toDto(stock, StockDTO.class))
      .toList();
    stocks.forEach(stock -> stock.setVendingMachineId(id));

    return stocks;
  }

  @Transactional(rollbackFor = { EntityNotFound.class, ValidationException.class })
  @Override
  public void provisionStock(final UUID id, final ItemDTO item, final Integer quantity)
      throws EntityNotFound, ValidationException
  {
    log.traceEntry(() -> id, () -> item, () -> quantity);

    VendingMachine machine = vendingMachineDao.read(id);
    Item itemToProvision = dtoMapper.toEntity(item, Item.class);

    ValidationResults validationResults = stockValidator.validate(itemToProvision, quantity, machine);
    validationResults.throwIfError("Cannot provision " + item + " to vending machine " + id);

    log.debug("Provisioning a quantity of {} of item '{}' to vending machine {}", quantity, item.getName(), id);

    if (!machine.hasItem(itemToProvision))
    {
      log.debug("Provisioning a new item to the vending machine {}: {} with quantity of {}", id, item, quantity);
      Stock stock = Stock.of(itemToProvision, quantity);
      machine.addStock(stock);
      dao.save(stock);
    }
    else
    {
      machine.getStocks()
        .stream()
        .filter(s -> Objects.equals(s.getItem(), itemToProvision))
        .findFirst()
        .ifPresent(s -> {
          s.addQuantity(quantity);
          log.debug("Provisioned stock of item '{}': stock quantity updated to {}", item.getName(), s.getQuantity());
          dao.save(s);
        });
    }

    machine.markIntervention();
    VendingMachine updatedMachine = vendingMachineDao.save(machine);

    log.info("Vending machine {} stock of '{}' increased by {}", updatedMachine.getId(), item.getName(), quantity);
  }
}
