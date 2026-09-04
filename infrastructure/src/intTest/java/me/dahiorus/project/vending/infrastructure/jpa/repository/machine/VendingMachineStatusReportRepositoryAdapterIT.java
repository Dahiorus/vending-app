package me.dahiorus.project.vending.infrastructure.jpa.repository.machine;

import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.CardSystemStatus.OK;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.ChangeSystemStatus.NORMAL;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.DEFAULT_TEMPERATURE;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.PowerStatus.POWER_OFF;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.WorkingStatus.WORKING;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.defaultStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import me.dahiorus.project.vending.domain.machine.entity.SerialNumber;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStatusReport;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStatusReportToCreate;
import me.dahiorus.project.vending.infrastructure.jpa.repository.H2DbContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

class VendingMachineStatusReportRepositoryAdapterIT extends H2DbContainer {

  @Autowired VendingMachineStatusReportRepositoryAdapter repository;

  @Test
  void should_create_status_report_of_given_vending_machine() {
    var reportToCreate =
        new VendingMachineStatusReportToCreate(
            SerialNumber.of("SN-1234-5678"),
            LocalDateTime.of(2025, 6, 9, 10, 35, 25),
            defaultStatus());

    var result = repository.create(reportToCreate);
    entityManager.flush();

    assertThat(result)
        .satisfies(
            report -> {
              assertThat(report.id()).isNotNull();
              assertThat(report.reportedAt()).isCloseTo(now(), within(200, MILLIS));
            })
        .usingRecursiveComparison()
        .ignoringFields("id", "reportedAt")
        .isEqualTo(
            new VendingMachineStatusReport(
                null,
                SerialNumber.of("SN-1234-5678"),
                LocalDateTime.of(2025, 6, 9, 10, 35, 25),
                new VendingMachineStatus(DEFAULT_TEMPERATURE, POWER_OFF, WORKING, OK, OK, NORMAL),
                null));
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    VendingMachineStatusReportRepositoryAdapter vendingMachineStatusReportJpaRepository(
        EntityManager entityManager) {
      return new VendingMachineStatusReportRepositoryAdapter(entityManager);
    }
  }
}
