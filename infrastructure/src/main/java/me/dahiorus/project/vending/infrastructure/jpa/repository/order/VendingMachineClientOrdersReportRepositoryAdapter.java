package me.dahiorus.project.vending.infrastructure.jpa.repository.order;

import java.util.Optional;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReport;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReportToCreate;
import me.dahiorus.project.vending.domain.reporting.port.VendingMachineClientOrdersReportRepositoryPort;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaClientOrdersReport;
import org.springframework.stereotype.Repository;

@Repository
public class VendingMachineClientOrdersReportRepositoryAdapter
    implements VendingMachineClientOrdersReportRepositoryPort {

  private final ClientOrdersReportJpaRepository jpaClientOrdersReportRepository;

  public VendingMachineClientOrdersReportRepositoryAdapter(
      final ClientOrdersReportJpaRepository jpaClientOrdersReportRepository) {
    this.jpaClientOrdersReportRepository = jpaClientOrdersReportRepository;
  }

  @Override
  public Optional<VendingMachineClientOrdersReport> findLastGeneratedOf(
      final VendingMachine vendingMachine) {
    return jpaClientOrdersReportRepository
        .findLastGeneratedOf(vendingMachine.serialNumber().value())
        .map(JpaClientOrdersReport::toDomain);
  }

  @Override
  public VendingMachineClientOrdersReport create(
      final VendingMachineClientOrdersReportToCreate toCreate) {
    JpaClientOrdersReport jpaClientOrdersReport = JpaClientOrdersReport.createFrom(toCreate);

    return jpaClientOrdersReportRepository.save(jpaClientOrdersReport).toDomain();
  }
}
