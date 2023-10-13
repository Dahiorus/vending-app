package me.dahiorus.project.vending.domain.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.dto.StockDto;
import me.dahiorus.project.vending.domain.model.dto.StockQuantityDto;
import me.dahiorus.project.vending.domain.service.DtoMapper;
import me.dahiorus.project.vending.domain.service.StockDtoService;
import me.dahiorus.project.vending.domain.service.manager.StockManager;
import me.dahiorus.project.vending.domain.service.validation.StockValidator;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;

@Transactional(readOnly = true)
@Log4j2
@RequiredArgsConstructor
@Service
public class StockDtoServiceImpl implements StockDtoService
{
  private final StockManager manager;

  private final StockValidator stockValidator;

  private final DtoMapper dtoMapper;

  @Override
  public List<StockDto> getStocks(final UUID id) throws EntityNotFound
  {
    VendingMachine entity = manager.getMachine(id);
    List<StockDto> stocks = entity.getStocks()
      .stream()
      .map(stock -> dtoMapper.toDto(stock, StockDto.class))
      .toList();
    stocks.forEach(stock -> stock.setVendingMachineId(id));

    return stocks;
  }

  @Transactional
  @Override
  public void provisionStock(final UUID id, final StockQuantityDto stockQuantity)
    throws EntityNotFound, ValidationException
  {
    log.traceEntry(() -> id, () -> stockQuantity);

    VendingMachine machine = manager.getMachine(id);
    Item itemToProvision = dtoMapper.toEntity(stockQuantity.item(), Item.class);

    ValidationResults validationResults = stockValidator.validate(stockQuantity, machine);
    validationResults.throwIfError("Cannot provision " + stockQuantity + " to vending machine " + id);

    manager.provision(machine, itemToProvision, stockQuantity.quantity());

    log.info("Vending machine {} stock of '{}' increased by {}", id,
      itemToProvision.getName(), stockQuantity.quantity());
  }
}
