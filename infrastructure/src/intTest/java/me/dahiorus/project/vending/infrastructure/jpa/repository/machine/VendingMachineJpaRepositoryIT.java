package me.dahiorus.project.vending.infrastructure.jpa.repository.machine;

import static me.dahiorus.project.vending.domain.item.entity.ItemType.COLD_BEVERAGE;
import static me.dahiorus.project.vending.domain.item.entity.ItemType.HOT_BEVERAGE;
import static me.dahiorus.project.vending.domain.item.entity.ItemType.SNACK;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.CardSystemStatus.OK;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.ChangeSystemStatus.NORMAL;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.PowerStatus.POWER_ON;
import static me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.WorkingStatus.WORKING;
import static me.dahiorus.project.vending.fixture.AddressFixture.anAddress;
import static me.dahiorus.project.vending.fixture.VendingMachineFixture.aVendingMachine;
import static me.dahiorus.project.vending.fixture.VendingMachineStatusFixture.aVendingMachineStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.machine.entity.Address;
import me.dahiorus.project.vending.domain.machine.entity.Address.City;
import me.dahiorus.project.vending.domain.machine.entity.Address.GeoCoordinates;
import me.dahiorus.project.vending.domain.machine.entity.Address.PostalCode;
import me.dahiorus.project.vending.domain.machine.entity.Address.StreetName;
import me.dahiorus.project.vending.domain.machine.entity.Address.StreetNumber;
import me.dahiorus.project.vending.domain.machine.entity.SerialNumber;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.Temperature;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineToUpdate;
import me.dahiorus.project.vending.domain.pagination.entity.Filter;
import me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher;
import me.dahiorus.project.vending.domain.pagination.entity.Pagination;
import me.dahiorus.project.vending.infrastructure.jpa.repository.H2DbContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = VendingMachineJpaRepositoryIT.TestConfig.class)
class VendingMachineJpaRepositoryIT extends H2DbContainer {
  @Autowired VendingMachineJpaRepository repository;

  @Test
  void should_create_vending_machine() {
    var vendingMachine =
        aVendingMachine()
            .id(null)
            .serialNumber("SN1234")
            .itemType(SNACK)
            .address(
                anAddress()
                    .coordinates(1.5235791, 48.5478201)
                    .city("Paris")
                    .streetNumber(4)
                    .streetName("Boulevard Beaumarchais")
                    .postalCode("75014")
                    .build())
            .status(
                aVendingMachineStatus()
                    .temperature(8)
                    .powerStatus(POWER_ON)
                    .workingStatus(WORKING)
                    .rfidStatus(OK)
                    .smartCardStatus(OK)
                    .changeMoneyStatus(NORMAL)
                    .build())
            .lastIntervention(null)
            .build();

    var result = repository.create(vendingMachine);
    entityManager.flush();

    assertThat(result)
        .satisfies(vm -> assertThat(vm.id()).isNotNull())
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(
            new VendingMachine(
                null,
                SerialNumber.of("SN1234"),
                new Address(
                    GeoCoordinates.of(1.5235791, 48.5478201),
                    StreetNumber.of(4),
                    StreetName.of("Boulevard Beaumarchais"),
                    PostalCode.of("75014"),
                    City.of("Paris")),
                SNACK,
                new VendingMachineStatus(Temperature.of(8), POWER_ON, WORKING, OK, OK, NORMAL),
                null));
  }

  @Nested
  class Find {
    @Test
    void should_get_vending_machine_by_id() {
      var vendingMachine =
          aVendingMachine()
              .id(new VendingMachineId(UUID.fromString("c29e78d0-e8fa-4c0e-82a9-0f05af4be3d2")))
              .build();
      repository.create(vendingMachine);
      entityManager.flush();

      var result =
          repository.find(
              new VendingMachineId(UUID.fromString("c29e78d0-e8fa-4c0e-82a9-0f05af4be3d2")));

      assertThat(result).contains(vendingMachine);
    }

    @Test
    void should_return_empty_when_vending_machine_not_found() {
      var result = repository.find(new VendingMachineId(UUID.randomUUID()));

      assertThat(result).isEmpty();
    }
  }

  @Nested
  class Update {
    @Test
    void should_update_given_vending_machine_by_id() {
      var vendingMachineCreated = repository.create(aVendingMachine().build());

      var result =
          repository.update(
              new VendingMachineToUpdate(
                  vendingMachineCreated.id(),
                  anAddress()
                      .streetNumber(50)
                      .streetName("Boulevard Voltaire")
                      .city("Asnières-sur-Seine")
                      .build(),
                  vendingMachineCreated.status(),
                  vendingMachineCreated.lastIntervention()));
      entityManager.flush();

      assertThat(result)
          .satisfies(
              vendingMachineUpdated -> {
                assertThat(vendingMachineUpdated.address())
                    .isEqualTo(
                        anAddress()
                            .streetNumber(50)
                            .streetName("Boulevard Voltaire")
                            .city("Asnières-sur-Seine")
                            .build());
                assertThat(vendingMachineUpdated.status())
                    .isEqualTo(vendingMachineCreated.status());
                assertThat(vendingMachineUpdated.lastIntervention())
                    .isEqualTo(vendingMachineCreated.lastIntervention());
              });
    }

    @Test
    void should_throw_exception_when_update_non_existent_vending_machine() {
      var vendingMachineId = new VendingMachineId(UUID.randomUUID());
      var vendingMachineToUpdate =
          new VendingMachineToUpdate(
              vendingMachineId, anAddress().build(), aVendingMachineStatus().build(), null);

      assertThatThrownBy(() -> repository.update(vendingMachineToUpdate))
          .isInstanceOf(ResourceNotFound.class)
          .hasMessageContaining("Resource not found with ID: " + vendingMachineId);
    }
  }

  @Test
  void should_delete_given_vending_machine() {
    var vendingMachineCreated =
        repository.create(
            aVendingMachine()
                .id(new VendingMachineId(UUID.fromString("c29e78d0-e8fa-4c0e-82a9-0f05af4be3d2")))
                .build());

    repository.delete(vendingMachineCreated.id());
    entityManager.flush();

    assertThat(repository.find(vendingMachineCreated.id())).isEmpty();
  }

  @Nested
  class FindDuplicateOf {
    @Test
    void should_find_duplicate_vending_machine_by_serial_number() {
      var serialNumber = "VM-1234";
      var vendingMachine =
          repository.create(aVendingMachine().id(null).serialNumber(serialNumber).build());
      entityManager.flush();

      var vendingMachineWithSameSerialNumber =
          aVendingMachine().id(null).serialNumber(serialNumber).build();

      var result = repository.findDuplicateOf(vendingMachineWithSameSerialNumber);

      assertThat(result).contains(vendingMachine);
    }

    @Test
    void should_not_return_self_when_finding_duplicate_vending_machine_by_serial_number() {
      var vendingMachine = repository.create(aVendingMachine().serialNumber("VM-1234").build());
      entityManager.flush();

      var result = repository.findDuplicateOf(vendingMachine);

      assertThat(result).isEmpty();
    }
  }

  @Nested
  class SearchAndCount {
    VendingMachine vendingMachine1, vendingMachine2, vendingMachine3;

    @BeforeEach
    void setUpVendingMachines() {
      vendingMachine1 = aVendingMachine().itemType(SNACK).serialNumber("VM-1234").build();
      vendingMachine2 = aVendingMachine().itemType(COLD_BEVERAGE).serialNumber("VM-4567").build();
      vendingMachine3 = aVendingMachine().itemType(HOT_BEVERAGE).serialNumber("VM-8910").build();
      repository.create(vendingMachine1);
      repository.create(vendingMachine2);
      repository.create(vendingMachine3);
      entityManager.flush();
    }

    @Nested
    class Search {
      @Test
      void should_search_vending_machines_by_filter() {
        var result =
            repository.search(
                new Pagination(),
                new Filter<>(
                    new VendingMachine(
                        null,
                        SerialNumber.of("VM-12"),
                        null,
                        SNACK,
                        aVendingMachineStatus().build(),
                        null),
                    new FilterMatcher()));

        assertThat(result).containsExactly(vendingMachine1);
      }

      @Test
      void should_list_all_given_empty_filter() {
        var result =
            repository.search(
                new Pagination(),
                new Filter<>(
                    new VendingMachine(null, null, null, null, null, null), new FilterMatcher()));

        assertThat(result).containsExactly(vendingMachine1, vendingMachine2, vendingMachine3);
      }
    }

    @Nested
    class Count {
      @Test
      void should_count_vending_machines_by_filter() {
        var result =
            repository.count(
                new Filter<>(
                    new VendingMachine(
                        null,
                        SerialNumber.of("VM-12"),
                        null,
                        SNACK,
                        aVendingMachineStatus().build(),
                        null),
                    new FilterMatcher()));

        assertThat(result).isEqualTo(1);
      }

      @Test
      void should_count_all_given_empty_filter() {
        var result =
            repository.count(
                new Filter<>(
                    new VendingMachine(null, null, null, null, null, null), new FilterMatcher()));

        assertThat(result).isEqualTo(3);
      }
    }
  }

  @Configuration
  static class TestConfig {
    @Bean
    VendingMachineJpaRepository repository(JpaVendingMachineDao jpaRepository) {
      return new VendingMachineJpaRepository(jpaRepository);
    }
  }
}
