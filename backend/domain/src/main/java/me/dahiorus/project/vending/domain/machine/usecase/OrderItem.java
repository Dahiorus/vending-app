package me.dahiorus.project.vending.domain.machine.usecase;

import me.dahiorus.project.vending.domain.documentation.DomainService;
import me.dahiorus.project.vending.domain.exception.ItemStockIsEmpty;
import me.dahiorus.project.vending.domain.exception.NotWorkingVendingMachine;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.item.entity.Item;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.port.ItemRepositoryPort;
import me.dahiorus.project.vending.domain.machine.entity.ClientOrder;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.port.ClientOrderRepositoryPort;
import me.dahiorus.project.vending.domain.machine.port.VendingMachineRepositoryPort;
import me.dahiorus.project.vending.domain.stock.entity.VendingMachineStock;
import me.dahiorus.project.vending.domain.stock.port.VendingMachineStockRepositoryPort;

@DomainService
public class OrderItem {
  private final VendingMachineRepositoryPort vendingMachineRepository;
  private final ItemRepositoryPort itemRepository;
  private final VendingMachineStockRepositoryPort vendingMachineStockRepository;
  private final ClientOrderRepositoryPort clientOrderRepository;

  public OrderItem(
      VendingMachineRepositoryPort vendingMachineRepository,
      ItemRepositoryPort itemRepository,
      VendingMachineStockRepositoryPort vendingMachineStockRepository,
      ClientOrderRepositoryPort clientOrderRepository) {
    this.vendingMachineRepository = vendingMachineRepository;
    this.itemRepository = itemRepository;
    this.vendingMachineStockRepository = vendingMachineStockRepository;
    this.clientOrderRepository = clientOrderRepository;
  }

  public ClientOrder execute(VendingMachineId vendingMachineId, ItemId itemId)
      throws ResourceNotFound, NotWorkingVendingMachine, ItemStockIsEmpty {
    var vendingMachine =
        vendingMachineRepository
            .find(vendingMachineId)
            .orElseThrow(() -> new ResourceNotFound(vendingMachineId));
    throwIfVendingMachineNotWorking(vendingMachine);

    var stock =
        vendingMachineStockRepository
            .find(vendingMachineId)
            .orElseThrow(() -> new ResourceNotFound(vendingMachineId));
    var item = itemRepository.find(itemId).orElseThrow(() -> new ResourceNotFound(itemId));
    throwIfItemStockIsEmpty(stock, item, vendingMachine);

    var stockToUpdate = stock.decrementStock(item);
    vendingMachineStockRepository.update(vendingMachineId, stockToUpdate);

    return clientOrderRepository.create(vendingMachine.id(), item.id());
  }

  private void throwIfVendingMachineNotWorking(VendingMachine vendingMachine)
      throws NotWorkingVendingMachine {
    if (!vendingMachine.isWorking()) {
      throw new NotWorkingVendingMachine(vendingMachine);
    }
  }

  private void throwIfItemStockIsEmpty(
      VendingMachineStock stock, Item item, VendingMachine vendingMachine) throws ItemStockIsEmpty {
    if (!stock.hasStock(item)) {
      throw new ItemStockIsEmpty(item, vendingMachine);
    }
  }
}
