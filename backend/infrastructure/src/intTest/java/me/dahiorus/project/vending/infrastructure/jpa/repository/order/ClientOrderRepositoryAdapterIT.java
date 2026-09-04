package me.dahiorus.project.vending.infrastructure.jpa.repository.order;

import static java.time.LocalDateTime.now;
import static java.time.Month.APRIL;
import static java.time.Month.JUNE;
import static java.time.Month.MAY;
import static java.util.Comparator.comparing;
import static java.util.UUID.randomUUID;
import static me.dahiorus.project.vending.domain.item.entity.ItemType.COLD_BEVERAGE;
import static me.dahiorus.project.vending.fixture.ItemFixture.aColdBeverage;
import static me.dahiorus.project.vending.fixture.VendingMachineFixture.aVendingMachine;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.item.entity.Item;
import me.dahiorus.project.vending.domain.machine.entity.ClientOrder;
import me.dahiorus.project.vending.domain.machine.entity.ClientOrder.OrderedItem;
import me.dahiorus.project.vending.domain.machine.entity.ClientOrderId;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = ClientOrderRepositoryAdapterIT.TestConfig.class)
class ClientOrderRepositoryAdapterIT extends H2DbContainer {

  @Autowired ClientOrderRepositoryAdapter clientOrderJpaRepository;

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
    void should_create_order_from_vending_machine_and_item() {
      var result = clientOrderJpaRepository.create(vendingMachine.id(), volvic33cl.id());

      assertThat(result)
          .usingRecursiveComparison()
          .ignoringFields("id", "orderAt")
          .isEqualTo(
              new ClientOrder(
                  new ClientOrderId(randomUUID()),
                  vendingMachine,
                  new OrderedItem(volvic33cl.id(), volvic33cl.name(), volvic33cl.price()),
                  now()));
      assertThat(result.id()).isNotNull();
      assertThat(result.orderAt()).isNotNull();
    }

    @Test
    void should_throw_exception_when_create_order_from_not_found_vending_machine() {
      assertThatThrownBy(
              () ->
                  clientOrderJpaRepository.create(
                      new VendingMachineId(randomUUID()), volvic33cl.id()))
          .isInstanceOf(ResourceNotFound.class);
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
      void should_find_all_orders_of_vending_machine_since_given_date_time() {
        var since = LocalDateTime.of(2025, JUNE, 2, 10, 30, 15);
        var result =
            clientOrderJpaRepository.findAllOfVendingMachineSince(vendingMachine.id(), since);

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
        assertThatThrownBy(
                () ->
                    clientOrderJpaRepository.findAllOfVendingMachineSince(
                        new VendingMachineId(randomUUID()), now()))
            .isInstanceOf(ResourceNotFound.class);
      }
    }

    @Nested
    class FindAll {
      @Test
      void should_find_all_orders_of_vending_machine() {
        var result = clientOrderJpaRepository.findAllOfVendingMachine(vendingMachine.id());

        assertThat(result)
            .hasSize(7)
            .isSortedAccordingTo(comparing(ClientOrder::orderAt).reversed());
      }

      @Test
      void should_throw_exception_when_vending_machine_not_found() {
        assertThatThrownBy(
                () ->
                    clientOrderJpaRepository.findAllOfVendingMachine(
                        new VendingMachineId(randomUUID())))
            .isInstanceOf(ResourceNotFound.class);
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
