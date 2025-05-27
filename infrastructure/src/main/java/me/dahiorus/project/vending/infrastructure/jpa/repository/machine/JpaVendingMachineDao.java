package me.dahiorus.project.vending.infrastructure.jpa.repository.machine;

import java.util.Optional;
import java.util.UUID;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaVendingMachine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaVendingMachineDao extends JpaRepository<JpaVendingMachine, UUID> {

  Optional<JpaVendingMachine> findBySerialNumber(String serialNumber);
}
