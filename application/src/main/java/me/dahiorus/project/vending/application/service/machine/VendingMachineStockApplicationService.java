package me.dahiorus.project.vending.application.service.machine;

import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.stock.entity.Quantity;
import me.dahiorus.project.vending.domain.stock.entity.VendingMachineStock;
import me.dahiorus.project.vending.domain.stock.port.VendingMachineStockApiPort;
import me.dahiorus.project.vending.domain.stock.port.VendingMachineStockRepositoryPort;
import me.dahiorus.project.vending.domain.stock.usecase.ProvisionVendingMachineStock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VendingMachineStockApplicationService implements VendingMachineStockApiPort {
  private final ProvisionVendingMachineStock provisionVendingMachineStock;
  private final VendingMachineStockRepositoryPort vendingMachineStockRepository;

  public VendingMachineStockApplicationService(
      ProvisionVendingMachineStock provisionVendingMachineStock,
      final VendingMachineStockRepositoryPort vendingMachineStockRepository) {
    this.provisionVendingMachineStock = provisionVendingMachineStock;
    this.vendingMachineStockRepository = vendingMachineStockRepository;
  }

  @Override
  public VendingMachineStock provision(
      VendingMachineId vendingMachineId, ItemId itemId, Quantity quantity) {
    return provisionVendingMachineStock.execute(vendingMachineId, itemId, quantity);
  }

  @Override
  public VendingMachineStock get(final VendingMachineId vendingMachineId) {
    return vendingMachineStockRepository
        .find(vendingMachineId)
        .orElseThrow(() -> new ResourceNotFound(vendingMachineId));
  }
}
