package me.dahiorus.project.vending.infrastructure.jpa.repository.machine;

import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static me.dahiorus.project.vending.fixture.ItemFixture.aSnack;
import static me.dahiorus.project.vending.fixture.VendingMachineStocksFixture.emptyStock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import jakarta.persistence.EntityManager;
import java.util.Set;
import me.dahiorus.project.vending.domain.item.entity.ItemName;
import me.dahiorus.project.vending.domain.machine.entity.SerialNumber;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStockReport;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStockReport.ReportedStockEntry;
import me.dahiorus.project.vending.domain.reporting.entity.VendingMachineStockReportToCreate;
import me.dahiorus.project.vending.domain.reporting.port.VendingMachineStockReportRepositoryPort;
import me.dahiorus.project.vending.domain.stock.entity.ItemQuantity;
import me.dahiorus.project.vending.domain.stock.entity.Quantity;
import me.dahiorus.project.vending.infrastructure.jpa.repository.H2DbContainer;
import me.dahiorus.project.vending.infrastructure.jpa.repository.machine.VendingMachineStockReportJpaRepositoryIT.TestConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestConfig.class)
class VendingMachineStockReportJpaRepositoryIT extends H2DbContainer {

  @Autowired VendingMachineStockReportRepositoryPort repository;

  @Nested
  class Create {
    @Test
    void should_create_stock_report() {
      // Given
      var stockReportToCreate =
          new VendingMachineStockReportToCreate(
              SerialNumber.of("SN123456"),
              emptyStock()
                  .addStock(new ItemQuantity(aSnack("Lays 80g", 1.8), Quantity.of(6)))
                  .addStock(new ItemQuantity(aSnack("Twix", 1.5), Quantity.of(5))));

      // When
      var result = repository.create(stockReportToCreate);
      entityManager.flush();

      // Then
      assertThat(result)
          .satisfies(
              stockReport -> {
                assertThat(stockReport.id()).isNotNull();
                assertThat(stockReport.reportedAt()).isCloseTo(now(), within(200, MILLIS));
              })
          .usingRecursiveComparison()
          .ignoringFields("id", "reportedAt")
          .isEqualTo(
              new VendingMachineStockReport(
                  null,
                  SerialNumber.of("SN123456"),
                  Set.of(
                      new ReportedStockEntry(ItemName.of("Lays 80g"), Quantity.of(6)),
                      new ReportedStockEntry(ItemName.of("Twix"), Quantity.of(5))),
                  null));
    }
  }

  @Configuration
  static class TestConfig {
    @Bean
    VendingMachineStockReportJpaRepository repository(EntityManager entityManager) {
      return new VendingMachineStockReportJpaRepository(entityManager);
    }
  }
}
