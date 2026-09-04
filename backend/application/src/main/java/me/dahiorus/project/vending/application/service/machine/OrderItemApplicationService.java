package me.dahiorus.project.vending.application.service.machine;

import me.dahiorus.project.vending.domain.exception.ItemStockIsEmpty;
import me.dahiorus.project.vending.domain.exception.NotWorkingVendingMachine;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.machine.entity.ClientOrder;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.port.OrderItemApiPort;
import me.dahiorus.project.vending.domain.machine.usecase.OrderItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderItemApplicationService implements OrderItemApiPort {
  private final OrderItem orderItemUseCase;

  public OrderItemApplicationService(OrderItem orderItemUseCase) {
    this.orderItemUseCase = orderItemUseCase;
  }

  @Override
  public ClientOrder orderItem(VendingMachineId vendingMachineId, ItemId itemId)
      throws ResourceNotFound, NotWorkingVendingMachine, ItemStockIsEmpty {
    return orderItemUseCase.execute(vendingMachineId, itemId);
  }
}
