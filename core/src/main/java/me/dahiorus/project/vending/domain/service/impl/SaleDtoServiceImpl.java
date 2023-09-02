package me.dahiorus.project.vending.domain.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ItemMissing;
import me.dahiorus.project.vending.domain.exception.VendingMachineNotWorking;
import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.Sale;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.dto.ItemDto;
import me.dahiorus.project.vending.domain.model.dto.SaleDto;
import me.dahiorus.project.vending.domain.service.DtoMapper;
import me.dahiorus.project.vending.domain.service.SaleDtoService;
import me.dahiorus.project.vending.domain.service.manager.SaleManager;

@Log4j2
@RequiredArgsConstructor
@Service
public class SaleDtoServiceImpl implements SaleDtoService
{
  private final SaleManager manager;

  private final DtoMapper dtoMapper;

  @Transactional
  @Override
  public SaleDto purchaseItem(final UUID id, final ItemDto item)
    throws EntityNotFound, ItemMissing, VendingMachineNotWorking
  {
    log.traceEntry(() -> id, () -> item);

    VendingMachine machine = manager.getWorkingMachine(id);
    Item itemToPurchase = dtoMapper.toEntity(item, Item.class);
    if (!machine.hasStock(itemToPurchase))
    {
      throw new ItemMissing("Vending machine " + id + " does not have item " + item.getName());
    }

    Sale sale = manager.purchaseItem(machine, itemToPurchase);

    log.info("Item '{}' purchased from vending machine {} for price {}", item.getName(),
      id, sale.getAmount());

    return log.traceExit(dtoMapper.toDto(sale, SaleDto.class));
  }
}
