package me.dahiorus.project.vending.infrastructure.jpa.repository.machine;

import jakarta.persistence.EntityManager;
import java.util.UUID;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStatusReport;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStatusReportToCreate;
import me.dahiorus.project.vending.domain.reporting.port.VendingMachineStatusReportRepositoryPort;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaStatusReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class VendingMachineStatusReportJpaRepository
    implements VendingMachineStatusReportRepositoryPort {
  private final JpaRepository<JpaStatusReport, UUID> jpaRepository;

  public VendingMachineStatusReportJpaRepository(final EntityManager entityManager) {
    this.jpaRepository = new SimpleJpaRepository<>(JpaStatusReport.class, entityManager);
  }

  @Override
  public VendingMachineStatusReport create(final VendingMachineStatusReportToCreate statusReport) {
    return jpaRepository.save(JpaStatusReport.createFrom(statusReport)).toDomain();
  }
}
