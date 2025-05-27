package me.dahiorus.project.vending.infrastructure.jpa.repository.order;

import java.util.Optional;
import java.util.UUID;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaClientOrdersReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaClientOrdersReportDao extends JpaRepository<JpaClientOrdersReport, UUID> {

  @Query(
      """
      FROM JpaClientOrdersReport clientOrdersReport
      WHERE clientOrdersReport.vendingMachineSerialNumber = :vendingMachineSerialNumber
      ORDER BY clientOrdersReport.createdAt DESC
      LIMIT 1
      """)
  Optional<JpaClientOrdersReport> findLastGeneratedOf(String vendingMachineSerialNumber);
}
