package me.dahiorus.project.vending.infrastructure.jpa.repository.order;

import static java.time.LocalDateTime.now;
import static java.time.Month.APRIL;
import static java.time.Month.JUNE;
import static java.time.Month.MAY;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Comparator.comparing;
import static java.util.UUID.randomUUID;
import static me.dahiorus.project.vending.domain.item.entity.ItemType.COLD_BEVERAGE;
import static me.dahiorus.project.vending.fixture.ItemFixture.aColdBeverage;
import static me.dahiorus.project.vending.fixture.VendingMachineFixture.aVendingMachine;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.within;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.item.entity.Item;
import me.dahiorus.project.vending.domain.machine.entity.ClientOrder;
import me.dahiorus.project.vending.domain.machine.entity.ClientOrder.OrderedItem;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.port.ClientOrderRepositoryPort;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaClientOrder;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaItem;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaVendingMachine;
import me.dahiorus.project.vending.infrastructure.jpa.repository.H2DbContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.spring.sql.QuickPerfSqlConfig;
import org.quickperf.sql.annotation.ExpectInsert;
import org.quickperf.sql.annotation.ExpectSelect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@QuickPerfTest
@Import(QuickPerfSqlConfig.class)
@ContextConfiguration(classes = ClientOrderRepositoryAdapterIT.TestConfig.class)
class ClientOrderRepositoryAdapterIT extends H2DbContainer {

  @Autowired ClientOrderRepositoryPort repository;

  VendingMachine vendingMachine;
  Item volvic33cl;

  @BeforeEach
  void setUp() {
    vendingMachine = aVendingMachine().itemType(COLD_BEVERAGE).build();
    volvic33cl = aColdBeverage("Volvic 33cL", 1.2);

    entityManager.persist(JpaVendingMachine.fromDomain(vendingMachine));
    entityManager.persist(JpaItem.fromDomain(volvic33cl));
    entityManager.flush();
  }

  @Nested
  class Create {
    @Test
    @ExpectInsert
    void should_create_order_from_vending_machine_and_item() {
      var result = repository.create(vendingMachine.id(), volvic33cl.id());
      entityManager.flush();

      assertThat(result)
          .usingRecursiveComparison()
          .ignoringFields("orderAt")
          .isEqualTo(
              new ClientOrder(
                  result.id(),
                  vendingMachine,
                  new OrderedItem(volvic33cl.id(), volvic33cl.name(), volvic33cl.price()),
                  now()));
      assertThat(result.orderAt()).isCloseTo(now(), within(200, MILLIS));
    }

    @Test
    void should_throw_exception_when_create_order_from_not_found_vending_machine() {
      var throwable =
          catchThrowable(
              () -> repository.create(new VendingMachineId(randomUUID()), volvic33cl.id()));
      assertThat(throwable).isInstanceOf(ResourceNotFound.class);
    }
  }

  @Nested
  class FindAllOfVendingMachine {

    @BeforeEach
    void setUpVendingMachineWithOrders() {
      var jpaVendingMachine = JpaVendingMachine.fromDomain(vendingMachine);
      var jpaItem = JpaItem.fromDomain(volvic33cl);
      var orderAtDates =
          List.of(
              LocalDateTime.of(2025, JUNE, 2, 10, 30, 15),
              LocalDateTime.of(2025, MAY, 10, 15, 20, 15),
              LocalDateTime.of(2025, JUNE, 5, 16, 30, 15),
              LocalDateTime.of(2025, JUNE, 7, 14, 50, 15),
              LocalDateTime.of(2025, APRIL, 12, 10, 0, 15),
              LocalDateTime.of(2025, MAY, 24, 10, 30, 15),
              LocalDateTime.of(2025, JUNE, 2, 11, 31, 17));

      orderAtDates.forEach(
          orderAt -> {
            var jpaClientOrder = new JpaClientOrder();
            jpaClientOrder.setItemDetails(jpaItem);
            jpaClientOrder.setOrderAt(orderAt);
            jpaVendingMachine.addOrder(jpaClientOrder);

            entityManager.persist(jpaClientOrder);
          });
      entityManager.merge(jpaVendingMachine);
      entityManager.flush();
    }

    @Nested
    class Since {

      @Test
      @ExpectSelect
      void should_find_all_orders_of_vending_machine_since_given_date_time() {
        var since = LocalDateTime.of(2025, JUNE, 2, 10, 30, 15);
        var result = repository.findAllOfVendingMachineSince(vendingMachine.id(), since);

        assertThat(result)
            .hasSize(4)
            .isSortedAccordingTo(comparing(ClientOrder::orderAt).reversed())
            .satisfies(
                clientOrders ->
                    clientOrders.forEach(
                        clientOrder -> assertThat(clientOrder.orderAt()).isAfterOrEqualTo(since)));
      }

      @Test
      void should_throw_exception_when_vending_machine_not_found() {
        var throwable =
            catchThrowable(
                () ->
                    repository.findAllOfVendingMachineSince(
                        new VendingMachineId(randomUUID()), now()));
        assertThat(throwable).isInstanceOf(ResourceNotFound.class);
      }
    }

    @Nested
    class FindAll {
      @Test
      @ExpectSelect
      void should_find_all_orders_of_vending_machine() {
        var result = repository.findAllOfVendingMachine(vendingMachine.id());

        assertThat(result)
            .hasSize(7)
            .isSortedAccordingTo(comparing(ClientOrder::orderAt).reversed());
      }

      @Test
      void should_throw_exception_when_vending_machine_not_found() {
        var throwable =
            catchThrowable(
                () -> repository.findAllOfVendingMachine(new VendingMachineId(randomUUID())));
        assertThat(throwable).isInstanceOf(ResourceNotFound.class);
      }
    }
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    public ClientOrderRepositoryPort clientOrderRepository(
        ClientOrderJpaRepository clientOrderDao, EntityManager entityManager) {
      return new ClientOrderRepositoryAdapter(clientOrderDao, entityManager);
    }
  }
}
