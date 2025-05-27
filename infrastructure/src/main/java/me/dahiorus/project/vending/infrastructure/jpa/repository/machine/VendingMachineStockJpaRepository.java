package me.dahiorus.project.vending.infrastructure.jpa.repository.machine;

import static java.util.function.Predicate.not;

import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.stock.entity.ItemQuantity;
import me.dahiorus.project.vending.domain.stock.entity.VendingMachineStock;
import me.dahiorus.project.vending.domain.stock.port.VendingMachineStockRepositoryPort;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaVendingMachine;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaVendingMachineStockEntry;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaVendingMachineStockEntry.JpaStockId;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;

@CacheConfig(cacheNames = "vendingMachineStocks")
@Repository
public class VendingMachineStockJpaRepository implements VendingMachineStockRepositoryPort {
  private final JpaRepository<JpaVendingMachine, UUID> jpaRepository;
  private final JpaRepository<JpaVendingMachineStockEntry, JpaStockId> jpaStockRepository;

  public VendingMachineStockJpaRepository(EntityManager entityManager) {
    this.jpaRepository = new SimpleJpaRepository<>(JpaVendingMachine.class, entityManager);
    this.jpaStockRepository =
        new SimpleJpaRepository<>(JpaVendingMachineStockEntry.class, entityManager);
  }

  @Cacheable(key = "#id.value")
  @Override
  public Optional<VendingMachineStock> find(VendingMachineId id) {
    return jpaRepository.findById(id.value()).map(JpaVendingMachine::toVendingMachineStocks);
  }

  @CachePut(key = "#id.value")
  @Override
  public VendingMachineStock update(VendingMachineId id, VendingMachineStock vendingMachineStock) {
    var vendingMachineToUpdate =
        jpaRepository.findById(id.value()).orElseThrow(() -> new ResourceNotFound(id));

    addOrUpdateStocks(
        vendingMachineStock.stream().filter(not(ItemQuantity::isEmpty)), vendingMachineToUpdate);
    removeEmptyStocks(
        vendingMachineStock.stream().filter(ItemQuantity::isEmpty), vendingMachineToUpdate);

    return jpaRepository.save(vendingMachineToUpdate).toVendingMachineStocks();
  }

  private void addOrUpdateStocks(
      Stream<ItemQuantity> stocksToUpdate, JpaVendingMachine vendingMachineToUpdate) {
    var vendingMachineId = new VendingMachineId(vendingMachineToUpdate.getId());
    stocksToUpdate
        .filter(not(ItemQuantity::isEmpty))
        .map(itemQuantity -> JpaVendingMachineStockEntry.fromDomain(vendingMachineId, itemQuantity))
        .forEach(
            jpaStock -> {
              vendingMachineToUpdate.addOrUpdateStock(jpaStock);
              jpaStockRepository.save(jpaStock);
            });
  }

  private void removeEmptyStocks(
      Stream<ItemQuantity> stocksToRemove, JpaVendingMachine vendingMachineToUpdate) {
    var vendingMachineId = new VendingMachineId(vendingMachineToUpdate.getId());
    stocksToRemove
        .filter(ItemQuantity::isEmpty)
        .map(itemQuantity -> JpaVendingMachineStockEntry.fromDomain(vendingMachineId, itemQuantity))
        .forEach(
            jpaStock -> {
              vendingMachineToUpdate.removeStock(jpaStock);
              jpaStockRepository.deleteById(jpaStock.getId());
            });
  }
}
