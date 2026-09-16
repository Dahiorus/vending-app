package me.dahiorus.project.vending.infrastructure.rest.controller.machine;

import static me.dahiorus.project.vending.domain.item.entity.ItemType.SNACK;
import static me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.CaseSensitivity.CASE_SENSITIVE;
import static me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.IgnoreOrIncludeNull.IGNORE;
import static me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.MatchAllOrAny.ANY;
import static me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.StringMatch.STARTING;
import static me.dahiorus.project.vending.fixture.VendingMachineFixture.aVendingMachine;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.machine.entity.SerialNumber;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus;
import me.dahiorus.project.vending.domain.machine.port.VendingMachineApiPort;
import me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher;
import me.dahiorus.project.vending.domain.pagination.entity.PageResult;
import me.dahiorus.project.vending.domain.pagination.entity.Pagination;
import me.dahiorus.project.vending.domain.pagination.entity.Pagination.Direction;
import me.dahiorus.project.vending.domain.pagination.entity.Total;
import me.dahiorus.project.vending.infrastructure.rest.assembler.VendingMachineDtoModelAssembler;
import me.dahiorus.project.vending.infrastructure.rest.assembler.VendingMachinePagedModelAssembler;
import me.dahiorus.project.vending.infrastructure.rest.exception.RestResponseExceptionHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VendingMachineCrudRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
  VendingMachineDtoModelAssembler.class,
  VendingMachinePagedModelAssembler.class,
  RestResponseExceptionHandler.class
})
class VendingMachineCrudRestControllerTest {
  @Autowired MockMvc mockMvc;

  @MockitoBean VendingMachineApiPort vendingMachineApiPort;

  @Test
  void should_create_vending_machine_and_return_location_from_self_link() throws Exception {
    // Given
    var createdMachine = aVendingMachine().build();

    given(vendingMachineApiPort.create(any())).willReturn(createdMachine);

    // When / Then
    mockMvc
        .perform(
            post("/api/v1/vending-machines")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "serialNumber": "VM-123456",
                      "itemType": "SNACK",
                      "address":  {
                        "latitude": 40.7128,
                        "longitude": -74.0060,
                        "streetNumber": 123,
                        "streetName": "Main St",
                        "postalCode": "10001",
                        "city": "New York"
                      }
                    }
                    """))
        .andExpectAll(
            status().isCreated(),
            header()
                .string(
                    "Location", "http://localhost/api/v1/vending-machines/" + idOf(createdMachine)),
            jsonPath("$.id").value(idOf(createdMachine).toString()),
            jsonPath("$.serialNumber").value(createdMachine.serialNumber().value()),
            jsonPath("$.itemType").value(createdMachine.itemType().name()),
            result -> {
              var address = createdMachine.address();
              jsonPath("$.address.latitude").value(address.coordinates().latitude());
              jsonPath("$.address.longitude").value(address.coordinates().longitude());
              jsonPath("$.address.streetNumber").value(address.streetNumber().value());
              jsonPath("$.address.streetName").value(address.streetName().value());
              jsonPath("$.address.postalCode").value(address.postalCode().value());
              jsonPath("$.address.city").value(address.city().value());
            });
  }

  @Test
  void should_read_vending_machine_by_id() throws Exception {
    // Given
    var machine = aVendingMachine().build();

    given(vendingMachineApiPort.read(machine.id())).willReturn(machine);

    // When / Then
    mockMvc
        .perform(get("/api/v1/vending-machines/{id}", idOf(machine)))
        .andExpectAll(
            status().isOk(),
            jsonPath("$.id").value(idOf(machine).toString()),
            jsonPath("$.serialNumber").value(machine.serialNumber().value()),
            jsonPath("$.itemType").value(machine.itemType().name()));
  }

  @Test
  void should_update_vending_machine_by_id() throws Exception {
    // Given
    var machine = aVendingMachine().build();

    given(vendingMachineApiPort.update(any())).willReturn(machine);

    // When / Then
    mockMvc
        .perform(
            put("/api/v1/vending-machines/{id}", idOf(machine))
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "itemType": "SNACK",
                      "address":  {
                        "latitude": 40.7128,
                        "longitude": -74.0060,
                        "streetNumber": 123,
                        "streetName": "Main St",
                        "postalCode": "10001",
                        "city": "New York"
                      },
                      "temperature": 4,
                      "powerStatus": "POWER_ON",
                      "workingStatus": "WORKING",
                      "rfidStatus": "OK",
                      "smartCardStatus": "OK",
                      "changeMoneyStatus": "NORMAL"
                    }
                    """))
        .andExpectAll(
            status().isOk(),
            jsonPath("$.id").value(idOf(machine).toString()),
            jsonPath("$.serialNumber").value(machine.serialNumber().value()),
            jsonPath("$.itemType").value(machine.itemType().name()));
  }

  @Test
  void should_delete_vending_machine_by_id() throws Exception {
    // Given
    var id = UUID.randomUUID();

    // When / Then
    mockMvc.perform(delete("/api/v1/vending-machines/{id}", id)).andExpect(status().isNoContent());

    then(vendingMachineApiPort).should().delete(new VendingMachineId(id));
  }

  @Test
  void should_search_vending_machines_with_pagination_and_filter_matcher() throws Exception {
    // Given
    var machine = aVendingMachine().build();

    var pageResult = new PageResult<>(List.of(machine), new Pagination(), new Total(11));

    given(vendingMachineApiPort.search(any(), any(), any())).willReturn(pageResult);

    // When / Then
    mockMvc
        .perform(
            get("/api/v1/vending-machines")
                .param("page", "2")
                .param("size", "5")
                .param("sort", "serialNumber,asc")
                .param("serialNumber", "SN-")
                .param("itemType", "SNACK")
                .param("stringMatch", "STARTING")
                .param("matchAllOrAny", "ANY")
                .param("caseSensitivity", "CASE_SENSITIVE"))
        .andExpectAll(status().isOk(),
            jsonPath("$._embedded.elements[0].id").value(idOf(machine).toString()),
            jsonPath("$._embedded.elements[0]._links.self.href")
                .value("http://localhost/api/v1/vending-machines/" + idOf(machine)),
            jsonPath("$.page.size").value(5),
            jsonPath("$.page.totalElements").value(11),
            jsonPath("$.page.totalPages").value(3),
            jsonPath("$.page.number").value(1),
            jsonPath("$._links.self.href").exists());

    var pagination = ArgumentCaptor.forClass(Pagination.class);
    var example = ArgumentCaptor.forClass(VendingMachine.class);
    var filterMatcher = ArgumentCaptor.forClass(FilterMatcher.class);
    then(vendingMachineApiPort)
        .should()
        .search(pagination.capture(), example.capture(), filterMatcher.capture());

    assertThat(pagination.getValue()).isEqualTo(new Pagination(1, 5, Map.of(Direction.ASC, Set.of("serialNumber"))));
    assertThat(filterMatcher.getValue()).isEqualTo(new FilterMatcher(STARTING, ANY, IGNORE, CASE_SENSITIVE));
    assertThat(example.getValue())
        .isEqualTo(
            new VendingMachine(
                new VendingMachineId(null),
                SerialNumber.of("SN-"),
                null,
                SNACK,
                new VendingMachineStatus(null, null, null, null, null, null),
                null));
  }

  @Test
  void should_return_bad_request_when_create_payload_cannot_be_deserialized() throws Exception {
    // Given / When / Then
    mockMvc
        .perform(
            post("/api/v1/vending-machines")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "serialNumber": "SN-1234",
                      "itemType": "COLD_BEVERAGE",
                      "address":  {}
                    }
                    """))
        .andExpect(status().isBadRequest());

    then(vendingMachineApiPort).shouldHaveNoInteractions();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidCreatePayloads")
  void should_reject_invalid_create_payload(
      String invalidField, String payload, String expectedCode) throws Exception {
    // Given / When / Then
    mockMvc
        .perform(post("/api/v1/vending-machines").contentType(APPLICATION_JSON).content(payload))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("1 errors found in [vendingMachineToCreateDto]"))
        .andExpect(jsonPath("$.errors[0].field").value(invalidField))
        .andExpect(jsonPath("$.errors[0].code").value(expectedCode));

    then(vendingMachineApiPort).shouldHaveNoInteractions();
  }

  static Stream<Arguments> invalidCreatePayloads() {
    return Stream.of(
        arguments(
            "serialNumber",
            """
            {
              "serialNumber": null,
              "itemType": "SNACK",
              "address":  {
                "latitude": 40.7128,
                "longitude": -74.0060,
                "streetNumber": 123,
                "streetName": "Main St",
                "postalCode": "10001",
                "city": "New York"
              }
            }
            """,
            "NotBlank"),
        arguments(
            "itemType",
            """
            {
              "serialNumber": "VM-123456",
              "itemType": null,
              "address":  {
                "latitude": 40.7128,
                "longitude": -74.0060,
                "streetNumber": 123,
                "streetName": "Main St",
                "postalCode": "10001",
                "city": "New York"
              }
            }
            """,
            "NotNull"),
        arguments(
            "address",
            """
            {
              "serialNumber": "VM-123456",
              "itemType": "SNACK",
              "address": null
            }
            """,
            "NotNull"),
        arguments(
            "address.latitude",
            """
            {
              "serialNumber": "VM-123456",
              "itemType": "SNACK",
              "address":  {
                "latitude": null,
                "longitude": -74.0060,
                "streetNumber": 123,
                "streetName": "Main St",
                "postalCode": "10001",
                "city": "New York"
              }
            }
            """,
            "NotNull"),
        arguments(
            "address.longitude",
            """
            {
              "serialNumber": "VM-123456",
              "itemType": "SNACK",
              "address":  {
                "latitude": 40.7128,
                "longitude": null,
                "streetNumber": 123,
                "streetName": "Main St",
                "postalCode": "10001",
                "city": "New York"
              }
            }
            """,
            "NotNull"),
        arguments(
            "address.streetNumber",
            """
            {
              "serialNumber": "VM-123456",
              "itemType": "SNACK",
              "address":  {
                "latitude": 40.7128,
                "longitude": -74.0060,
                "streetNumber": -1,
                "streetName": "Main St",
                "postalCode": "10001",
                "city": "New York"
              }
            }
            """,
            "Positive"),
        arguments(
            "address.streetName",
            """
            {
              "serialNumber": "VM-123456",
              "itemType": "SNACK",
              "address":  {
                "latitude": 40.7128,
                "longitude": -74.0060,
                "streetNumber": 25,
                "streetName": "  ",
                "postalCode": "10001",
                "city": "New York"
              }
            }
            """,
            "NotBlank"),
        arguments(
            "address.postalCode",
            """
            {
              "serialNumber": "VM-123456",
              "itemType": "SNACK",
              "address":  {
                "latitude": 40.7128,
                "longitude": -74.0060,
                "streetNumber": 25,
                "streetName": "Main Street",
                "postalCode": "",
                "city": "New York"
              }
            }
            """,
            "NotBlank"),
        arguments(
            "address.city",
            """
            {
              "serialNumber": "VM-123456",
              "itemType": "SNACK",
              "address":  {
                "latitude": 40.7128,
                "longitude": -74.0060,
                "streetNumber": 25,
                "streetName": "Main Street",
                "postalCode": "10001",
                "city": null
              }
            }
            """,
            "NotBlank"));
  }

  @Test
  void should_translate_resource_not_found_to_not_found_response() throws Exception {
    // Given
    var id = UUID.randomUUID();
    given(vendingMachineApiPort.read(new VendingMachineId(id)))
        .willThrow(new ResourceNotFound(new VendingMachineId(id)));

    // When / Then
    mockMvc
        .perform(get("/api/v1/vending-machines/{id}", id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Resource not found with ID: " + new VendingMachineId(id)));
  }

  private static UUID idOf(VendingMachine vendingMachine) {
    return vendingMachine.id().value();
  }
}
