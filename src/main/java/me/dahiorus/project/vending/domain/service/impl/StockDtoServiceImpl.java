package me.dahiorus.project.vending.domain.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.dao.DAO;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.Stock;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.dto.ItemDTO;
import me.dahiorus.project.vending.domain.model.dto.StockDTO;
import me.dahiorus.project.vending.domain.service.DtoMapper;
import me.dahiorus.project.vending.domain.service.StockDtoService;
import me.dahiorus.project.vending.domain.service.validation.StockValidator;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;

@Transactional(readOnly = true)
@Log4j2
@RequiredArgsConstructor
@Service
public class StockDtoServiceImpl implements StockDtoService
{
  private final DAO<Stock> dao;

  private final DAO<VendingMachine> vendingMachineDao;

  private final StockValidator stockValidator;

  private final DtoMapper dtoMapper;

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

  @Transactional
  @Override
  public void provisionStock(final UUID id, final ItemDTO item, final Integer quantity)
    throws EntityNotFound, ValidationException
  {
    log.traceEntry(() -> id, () -> item, () -> quantity);

    VendingMachine machine = vendingMachineDao.read(id);
    Item itemToProvision = dtoMapper.toEntity(item, Item.class);

    ValidationResults validationResults = stockValidator.validate(itemToProvision, quantity, machine);
    validationResults.throwIfError("Cannot provision " + item + " to vending machine " + id);

    log.debug("Provisioning a quantity of {} of item '{}' to vending machine {}", quantity,
      item.getName(), id);

    if (!machine.hasItem(itemToProvision))
    {
      log.debug("Provisioning a new item to the vending machine {}: {} with quantity of {}", id,
        item, quantity);
      Stock stock = Stock.fill(itemToProvision, quantity);
      machine.addStock(stock);
      dao.save(stock);
    }
    else
    {
      machine.getStock(itemToProvision)
        .ifPresent(s -> {
          s.addQuantity(quantity);
          log.debug("Provisioned stock of item '{}': stock quantity updated to {}", item.getName(),
            s.getQuantity());
          dao.save(s);
        });
    }

    machine.markIntervention();
    VendingMachine updatedMachine = vendingMachineDao.save(machine);

    log.info("Vending machine {} stock of '{}' increased by {}", updatedMachine.getId(),
      item.getName(), quantity);
  }
}
