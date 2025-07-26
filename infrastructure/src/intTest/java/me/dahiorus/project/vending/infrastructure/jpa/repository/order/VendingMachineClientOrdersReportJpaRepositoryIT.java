package me.dahiorus.project.vending.infrastructure.jpa.repository.order;

import static java.time.Duration.ofSeconds;
import static java.time.LocalDateTime.now;
import static java.time.Month.JUNE;
import static java.time.Month.MAY;
import static java.time.temporal.ChronoUnit.MILLIS;
import static me.dahiorus.project.vending.fixture.ClientOrderFixture.aClientOrder;
import static me.dahiorus.project.vending.fixture.VendingMachineFixture.aVendingMachine;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import me.dahiorus.project.vending.domain.item.entity.ItemName;
import me.dahiorus.project.vending.domain.machine.entity.SerialNumber;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReport;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReport.ReportedClientOrder;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineClientOrdersReportToCreate;
import me.dahiorus.project.vending.infrastructure.jpa.repository.H2DbContainer;
import me.dahiorus.project.vending.infrastructure.jpa.repository.order.VendingMachineClientOrdersReportJpaRepositoryIT.TestConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestConfig.class)
class VendingMachineClientOrdersReportJpaRepositoryIT extends H2DbContainer {

  @Autowired VendingMachineClientOrdersReportJpaRepository repository;

  @Nested
  class Create {
    @Test
    void should_create_client_orders_report() {
      // Given
      var vendingMachine = aVendingMachine().serialNumber("VM-123-456").build();
      var clientOrders =
          List.of(
              aClientOrder(
                  vendingMachine,
                  "Coke",
                  BigDecimal.valueOf(1.5),
                  LocalDateTime.of(2025, MAY, 5, 9, 0, 26)),
              aClientOrder(
                  vendingMachine,
                  "Pepsi",
                  BigDecimal.valueOf(1.5),
                  LocalDateTime.of(2025, JUNE, 1, 11, 0, 26)));

      // When
      var result =
          repository.create(
              new VendingMachineClientOrdersReportToCreate(vendingMachine, clientOrders));
      entityManager.flush();

      // Then
      assertThat(result)
          .usingRecursiveComparison()
          .ignoringFields("id", "reportedAt")
          .isEqualTo(
              new VendingMachineClientOrdersReport(
                  null,
                  vendingMachine.serialNumber(),
                  List.of(
                      new ReportedClientOrder(
                          SerialNumber.of("VM-123-456"),
                          ItemName.of("Pepsi"),
                          BigDecimal.valueOf(1.5),
                          LocalDateTime.of(2025, JUNE, 1, 11, 0, 26)),
                      new ReportedClientOrder(
                          SerialNumber.of("VM-123-456"),
                          ItemName.of("Coke"),
                          BigDecimal.valueOf(1.5),
                          LocalDateTime.of(2025, MAY, 5, 9, 0, 26))),
                  null));
      assertThat(result.reportedAt()).isCloseTo(now(), within(200, MILLIS));
      assertThat(result.id()).isNotNull();
    }
  }

  @Nested
  class FindLastGeneratedOf {

    @Test
    void should_return_empty_when_no_report_exists() {
      // Given
      var vendingMachine = aVendingMachine().serialNumber("VM-123-456").build();

      // When
      var result = repository.findLastGeneratedOf(vendingMachine);

      // Then
      assertThat(result).isEmpty();
    }

    @Test
    void should_return_last_generated_report_of_given_vending_machine() {
      // Given
      var vendingMachine = aVendingMachine().serialNumber("VM-123-456").build();
      var clientOrders =
          List.of(
              aClientOrder(
                  vendingMachine,
                  "Coke",
                  BigDecimal.valueOf(1.5),
                  LocalDateTime.of(2025, MAY, 5, 9, 0, 26)),
              aClientOrder(
                  vendingMachine,
                  "Pepsi",
                  BigDecimal.valueOf(1.5),
                  LocalDateTime.of(2025, JUNE, 1, 11, 0, 26)));

      repository.create(new VendingMachineClientOrdersReportToCreate(vendingMachine, clientOrders));
      await().atLeast(ofSeconds(6));
      repository.create(
          new VendingMachineClientOrdersReportToCreate(
              vendingMachine,
              List.of(
                  aClientOrder(
                      vendingMachine,
                      "Coke",
                      BigDecimal.valueOf(1.5),
                      LocalDateTime.of(2025, JUNE, 20, 9, 0, 26)))));
      entityManager.flush();

      var now = LocalDateTime.now();
      await().atLeast(ofSeconds(5));

      // When
      var result = repository.findLastGeneratedOf(vendingMachine);

      // Then
      assertThat(result)
          .get()
          .usingRecursiveComparison()
          .ignoringFields("id", "reportedAt")
          .isEqualTo(
              new VendingMachineClientOrdersReport(
                  null,
                  vendingMachine.serialNumber(),
                  List.of(
                      new ReportedClientOrder(
                          SerialNumber.of("VM-123-456"),
                          ItemName.of("Coke"),
                          BigDecimal.valueOf(1.5),
                          LocalDateTime.of(2025, JUNE, 20, 9, 0, 26))),
                  null))
          .asInstanceOf(type(VendingMachineClientOrdersReport.class))
          .satisfies(report -> assertThat(report.reportedAt()).isCloseTo(now, within(200, MILLIS)));
    }
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    VendingMachineClientOrdersReportJpaRepository vendingMachineClientOrdersReportJpaRepository(
        JpaClientOrdersReportDao jpaClientOrdersReportDao) {
      return new VendingMachineClientOrdersReportJpaRepository(jpaClientOrdersReportDao);
    }
  }
}
