package me.dahiorus.project.vending.infrastructure.jpa.repository.machine;

import jakarta.persistence.EntityManager;
import java.util.UUID;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStockReport;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStockReportToCreate;
import me.dahiorus.project.vending.domain.reporting.port.VendingMachineStockReportRepositoryPort;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaStockReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class VendingMachineStockReportJpaRepository
    implements VendingMachineStockReportRepositoryPort {

  private final JpaRepository<JpaStockReport, UUID> jpaRepository;

  public VendingMachineStockReportJpaRepository(final EntityManager entityManager) {
    this.jpaRepository = new SimpleJpaRepository<>(JpaStockReport.class, entityManager);
  }

  @Override
  public VendingMachineStockReport create(final VendingMachineStockReportToCreate toCreate) {
    return jpaRepository.save(JpaStockReport.createFrom(toCreate)).toDomain();
  }
}
