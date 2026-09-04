package me.dahiorus.project.vending.domain.stock.usecase;

import static java.time.LocalDateTime.now;

import java.time.Clock;
import me.dahiorus.project.vending.domain.documentation.DomainService;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.exception.UnsupportedItemToProvision;
import me.dahiorus.project.vending.domain.item.entity.Item;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.port.ItemRepositoryPort;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.port.VendingMachineRepositoryPort;
import me.dahiorus.project.vending.domain.stock.entity.ItemQuantity;
import me.dahiorus.project.vending.domain.stock.entity.Quantity;
import me.dahiorus.project.vending.domain.stock.entity.VendingMachineStock;
import me.dahiorus.project.vending.domain.stock.port.VendingMachineStockRepositoryPort;

@DomainService
public class ProvisionVendingMachineStock {
  private final Clock clock;
  private final VendingMachineRepositoryPort vendingMachineRepository;
  private final ItemRepositoryPort itemRepository;
  private final VendingMachineStockRepositoryPort vendingMachineStockRepository;

  public ProvisionVendingMachineStock(
      Clock clock,
      VendingMachineRepositoryPort vendingMachineRepository,
      ItemRepositoryPort itemRepository,
      VendingMachineStockRepositoryPort vendingMachineStockRepository) {
    this.clock = clock;
    this.vendingMachineRepository = vendingMachineRepository;
    this.itemRepository = itemRepository;
    this.vendingMachineStockRepository = vendingMachineStockRepository;
  }

  public VendingMachineStock execute(VendingMachineId machineId, ItemId itemId, Quantity quantity)
      throws ResourceNotFound, UnsupportedItemToProvision {
    var vendingMachine =
        vendingMachineRepository.find(machineId).orElseThrow(() -> new ResourceNotFound(machineId));
    var item = itemRepository.find(itemId).orElseThrow(() -> new ResourceNotFound(itemId));

    throwIfUnsupportedItemType(item, vendingMachine);

    var machineStocks =
        vendingMachineStockRepository
            .find(machineId)
            .orElseThrow(() -> new ResourceNotFound(machineId));

    vendingMachineRepository.update(vendingMachine.markIntervention(now(clock)).toUpdate());

    var updatedMachineStocks = machineStocks.addStock(new ItemQuantity(item, quantity));
    vendingMachineStockRepository.update(machineId, updatedMachineStocks);

    return updatedMachineStocks;
  }

  private void throwIfUnsupportedItemType(Item item, VendingMachine vendingMachine)
      throws UnsupportedItemToProvision {
    if (!vendingMachine.supports(item)) {
      throw new UnsupportedItemToProvision(item, vendingMachine);
    }
  }
}
