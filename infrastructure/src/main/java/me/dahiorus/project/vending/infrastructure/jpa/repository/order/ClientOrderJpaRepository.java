package me.dahiorus.project.vending.infrastructure.jpa.repository.order;

import static java.util.Comparator.comparing;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.machine.entity.ClientOrder;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.port.ClientOrderRepositoryPort;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaClientOrder;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaItem;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaVendingMachine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ClientOrderJpaRepository implements ClientOrderRepositoryPort {
  private final JpaClientOrderDao clientOrderRepository;
  private final JpaRepository<JpaVendingMachine, UUID> vendingMachineRepository;
  private final JpaRepository<JpaItem, UUID> itemRepository;

  public ClientOrderJpaRepository(
      JpaClientOrderDao clientOrderRepository, EntityManager entityManager) {
    this.clientOrderRepository = clientOrderRepository;
    vendingMachineRepository = new SimpleJpaRepository<>(JpaVendingMachine.class, entityManager);
    itemRepository = new SimpleJpaRepository<>(JpaItem.class, entityManager);
  }

  @Override
  public ClientOrder create(final VendingMachineId vendingMachineId, final ItemId itemId) {
    var vendingMachineToOrder =
        vendingMachineRepository
            .findById(vendingMachineId.value())
            .orElseThrow(() -> new ResourceNotFound(vendingMachineId));
    var itemToOrder =
        itemRepository.findById(itemId.value()).orElseThrow(() -> new ResourceNotFound(itemId));
    var jpaClientOrder = toJpaClientOrder(vendingMachineToOrder, itemToOrder);

    vendingMachineToOrder.addOrder(jpaClientOrder);
    jpaClientOrder = clientOrderRepository.save(jpaClientOrder);
    vendingMachineRepository.save(vendingMachineToOrder);

    return jpaClientOrder.toDomain();
  }

  private static JpaClientOrder toJpaClientOrder(
      final JpaVendingMachine vendingMachineToOrder, final JpaItem itemToOrder) {
    var order = new JpaClientOrder();
    order.setVendingMachine(vendingMachineToOrder);
    order.setItemDetails(itemToOrder);

    return order;
  }

  @Override
  public List<ClientOrder> findAllOfVendingMachine(final VendingMachineId vendingMachineId) {
    var jpaVendingMachine =
        vendingMachineRepository
            .findById(vendingMachineId.value())
            .orElseThrow(() -> new ResourceNotFound(vendingMachineId));

    return clientOrderRepository
        .findAllByVendingMachineOrderByCreatedAtDesc(jpaVendingMachine)
        .stream()
        .map(JpaClientOrder::toDomain)
        .sorted(comparing(ClientOrder::orderAt).reversed())
        .toList();
  }

  @Override
  public List<ClientOrder> findAllOfVendingMachineSince(
      VendingMachineId vendingMachineId, LocalDateTime since) throws ResourceNotFound {
    var vendingMachine =
        vendingMachineRepository
            .findById(vendingMachineId.value())
            .orElseThrow(() -> new ResourceNotFound(vendingMachineId));

    return clientOrderRepository.findAllClientOrdersSince(vendingMachine, since).stream()
        .map(JpaClientOrder::toDomain)
        .toList();
  }
}
