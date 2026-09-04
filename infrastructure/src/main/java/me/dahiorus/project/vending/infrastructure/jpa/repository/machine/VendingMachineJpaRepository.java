package me.dahiorus.project.vending.infrastructure.jpa.repository.machine;

import static java.util.function.Predicate.not;
import static me.dahiorus.project.vending.infrastructure.jpa.repository.ExampleMatcherAdapter.toExample;
import static me.dahiorus.project.vending.infrastructure.jpa.repository.ToPageableConverter.toPageable;

import java.util.List;
import java.util.Optional;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineToUpdate;
import me.dahiorus.project.vending.domain.machine.port.VendingMachineRepositoryPort;
import me.dahiorus.project.vending.domain.pagination.entity.Filter;
import me.dahiorus.project.vending.domain.pagination.entity.Pagination;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaVendingMachine;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@CacheConfig(cacheNames = "vendingMachines")
@Repository
public class VendingMachineJpaRepository implements VendingMachineRepositoryPort {
  private final JpaVendingMachineDao jpaRepository;

  public VendingMachineJpaRepository(JpaVendingMachineDao jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @CachePut(key = "#result.id.value")
  @Override
  public VendingMachine create(VendingMachine machineToCreate) {
    return jpaRepository.save(JpaVendingMachine.fromDomain(machineToCreate)).toDomain();
  }

  @Cacheable(key = "#id.value")
  @Override
  public Optional<VendingMachine> find(VendingMachineId id) {
    return jpaRepository.findById(id.value()).map(JpaVendingMachine::toDomain);
  }

  @CachePut(key = "#result.id.value")
  @Override
  public VendingMachine update(VendingMachineToUpdate toUpdate) {
    return find(toUpdate.id())
        .map(machine -> machine.updateFrom(toUpdate))
        .map(JpaVendingMachine::fromDomain)
        .map(jpaRepository::save)
        .map(JpaVendingMachine::toDomain)
        .orElseThrow(() -> new ResourceNotFound(toUpdate.id()));
  }

  @CacheEvict(key = "#id.value")
  @Override
  public void delete(VendingMachineId id) {
    jpaRepository.deleteById(id.value());
  }

  @Override
  public List<VendingMachine> search(Pagination pagination, Filter<VendingMachine> filter) {
    var pageable = toPageable(pagination);
    var example = toExample(filter, JpaVendingMachine::fromDomain);

    return jpaRepository.findAll(example, pageable).map(JpaVendingMachine::toDomain).toList();
  }

  @Override
  public long count(Filter<VendingMachine> filter) {
    var example = toExample(filter, JpaVendingMachine::fromDomain);

    return jpaRepository.count(example);
  }

  @Override
  public Optional<VendingMachine> findDuplicateOf(VendingMachine vendingMachine) {
    return jpaRepository
        .findBySerialNumber(vendingMachine.serialNumber().value())
        .map(JpaVendingMachine::toDomain)
        .filter(not(duplicate -> duplicate.id().equals(vendingMachine.id())));
  }
}
