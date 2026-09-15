package me.dahiorus.project.vending.infrastructure.rest.controller.item;

import static me.dahiorus.project.vending.domain.item.entity.ItemType.COLD_BEVERAGE;
import static me.dahiorus.project.vending.domain.item.entity.ItemType.SNACK;
import static me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.CaseSensitivity.CASE_SENSITIVE;
import static me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.IgnoreOrIncludeNull.INCLUDE;
import static me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.MatchAllOrAny.ANY;
import static me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher.StringMatch.STARTING;
import static me.dahiorus.project.vending.fixture.ItemFixture.aColdBeverage;
import static me.dahiorus.project.vending.fixture.ItemFixture.aSnack;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
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
import java.util.UUID;
import java.util.stream.Stream;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.item.entity.Item;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.entity.ItemToCreate;
import me.dahiorus.project.vending.domain.item.entity.ItemToUpdate;
import me.dahiorus.project.vending.domain.item.port.ItemApiPort;
import me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher;
import me.dahiorus.project.vending.domain.pagination.entity.PageResult;
import me.dahiorus.project.vending.domain.pagination.entity.PageSort.SortProperty;
import me.dahiorus.project.vending.domain.pagination.entity.Pagination;
import me.dahiorus.project.vending.domain.pagination.entity.Total;
import me.dahiorus.project.vending.infrastructure.rest.assembler.ItemDtoModelAssembler;
import me.dahiorus.project.vending.infrastructure.rest.assembler.ItemPagedModelAssembler;
import me.dahiorus.project.vending.infrastructure.rest.exception.RestResponseExceptionHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ItemCrudRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
  ItemDtoModelAssembler.class,
  ItemPagedModelAssembler.class,
  RestResponseExceptionHandler.class
})
class ItemCrudRestControllerTest {
  @Autowired MockMvc mockMvc;

  @MockitoBean ItemApiPort itemApiPort;

  @Test
  void should_create_item_and_return_location_from_self_link() throws Exception {
    // Given
    var createdItem = aColdBeverage("Water", 1.5);

    given(itemApiPort.create(any(ItemToCreate.class))).willReturn(createdItem);

    // When / Then
    mockMvc
        .perform(
            post("/api/v1/items")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "Water",
                      "type": "COLD_BEVERAGE",
                      "price": 1.5
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(
            header().string("Location", "http://localhost/api/v1/items/" + idOf(createdItem)))
        .andExpect(jsonPath("$.id").value(idOf(createdItem).toString()))
        .andExpect(jsonPath("$.name").value("Water"))
        .andExpect(jsonPath("$.type").value("COLD_BEVERAGE"))
        .andExpect(jsonPath("$.price").value(1.5))
        .andExpect(
            jsonPath("$._links.self.href")
                .value("http://localhost/api/v1/items/" + idOf(createdItem)));

    var itemToCreate = ArgumentCaptor.forClass(ItemToCreate.class);
    then(itemApiPort).should().create(itemToCreate.capture());
    assertThat(itemToCreate.getValue().name().value()).isEqualTo("Water");
    assertThat(itemToCreate.getValue().type()).isEqualTo(COLD_BEVERAGE);
    assertThat(itemToCreate.getValue().price()).isEqualByComparingTo("1.5");
  }

  @Test
  void should_read_item_by_id() throws Exception {
    // Given
    var item = aSnack("Chips", 2.1);

    given(itemApiPort.read(item.id())).willReturn(item);

    // When / Then
    mockMvc
        .perform(get("/api/v1/items/{id}", idOf(item)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(idOf(item).toString()))
        .andExpect(jsonPath("$.name").value("Chips"))
        .andExpect(jsonPath("$.type").value("SNACK"))
        .andExpect(jsonPath("$.price").value(2.1))
        .andExpect(
            jsonPath("$._links.self.href").value("http://localhost/api/v1/items/" + idOf(item)));
  }

  @Test
  void should_update_item_by_id() throws Exception {
    // Given
    var item = aSnack("Chips", 2.4);

    given(itemApiPort.update(any(ItemToUpdate.class))).willReturn(item);

    // When / Then
    mockMvc
        .perform(
            put("/api/v1/items/{id}", idOf(item))
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "price": 2.4
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(idOf(item).toString()))
        .andExpect(jsonPath("$.price").value(2.4))
        .andExpect(
            jsonPath("$._links.self.href").value("http://localhost/api/v1/items/" + idOf(item)));

    var itemToUpdate = ArgumentCaptor.forClass(ItemToUpdate.class);
    then(itemApiPort).should().update(itemToUpdate.capture());
    assertThat(itemToUpdate.getValue().id()).isEqualTo(item.id());
    assertThat(itemToUpdate.getValue().price()).isEqualByComparingTo("2.4");
  }

  @Test
  void should_delete_item_by_id() throws Exception {
    // Given
    var id = UUID.randomUUID();

    // When / Then
    mockMvc.perform(delete("/api/v1/items/{id}", id)).andExpect(status().isNoContent());

    then(itemApiPort).should().delete(new ItemId(id));
  }

  @Test
  void should_search_items_with_pagination_example_and_filter_matcher() throws Exception {
    // Given
    var item = aSnack("Chips", 2.25);
    var pageResult = new PageResult<>(List.of(item), new Pagination(), new Total(11));

    given(itemApiPort.search(any(Pagination.class), any(Item.class), any(FilterMatcher.class)))
        .willReturn(pageResult);

    // When / Then
    mockMvc
        .perform(
            get("/api/v1/items")
                .param("page", "2")
                .param("size", "5")
                .param("sort", "name,desc")
                .param("name", "Ch")
                .param("type", "SNACK")
                .param("price", "2.25")
                .param("stringMatch", "STARTING")
                .param("matchAllOrAny", "ANY")
                .param("ignoreOrIncludeNull", "INCLUDE")
                .param("caseSensitivity", "CASE_SENSITIVE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.elements[0].id").value(idOf(item).toString()))
        .andExpect(
            jsonPath("$._embedded.elements[0]._links.self.href")
                .value("http://localhost/api/v1/items/" + idOf(item)))
        .andExpect(jsonPath("$.page.size").value(5))
        .andExpect(jsonPath("$.page.totalElements").value(11))
        .andExpect(jsonPath("$.page.totalPages").value(3))
        .andExpect(jsonPath("$.page.number").value(1))
        .andExpect(jsonPath("$._links.self.href").exists());

    var pagination = ArgumentCaptor.forClass(Pagination.class);
    var example = ArgumentCaptor.forClass(Item.class);
    var filterMatcher = ArgumentCaptor.forClass(FilterMatcher.class);
    then(itemApiPort)
        .should()
        .search(pagination.capture(), example.capture(), filterMatcher.capture());

    assertThat(pagination.getValue().pageNumber()).isEqualTo(1);
    assertThat(pagination.getValue().pageSize()).isEqualTo(5);
    assertThat(pagination.getValue().sort().sortProperties())
        .containsExactly(SortProperty.desc("name"));
    assertThat(example.getValue().id()).isNull();
    assertThat(example.getValue().name().value()).isEqualTo("Ch");
    assertThat(example.getValue().price()).isEqualByComparingTo("2.25");
    assertThat(example.getValue().type()).isEqualTo(SNACK);
    assertThat(filterMatcher.getValue().stringMatch()).isEqualTo(STARTING);
    assertThat(filterMatcher.getValue().matchAllOrAny()).isEqualTo(ANY);
    assertThat(filterMatcher.getValue().ignoreOrIncludeNull()).isEqualTo(INCLUDE);
    assertThat(filterMatcher.getValue().caseSensitivity()).isEqualTo(CASE_SENSITIVE);
  }

  @Test
  void should_return_bad_request_when_create_payload_cannot_be_deserialized() throws Exception {
    // Given / When / Then
    mockMvc
        .perform(
            post("/api/v1/items")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "Water",
                      "type": "UNKNOWN",
                      "price": 1.5
                    }
                    """))
        .andExpect(status().isBadRequest());

    then(itemApiPort).shouldHaveNoInteractions();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidCreatePayloads")
  void should_reject_invalid_create_payload(
      String invalidField, String payload, String expectedCode) throws Exception {
    // Given / When / Then
    mockMvc
        .perform(post("/api/v1/items").contentType(APPLICATION_JSON).content(payload))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("1 errors found in [itemToCreateDto]"))
        .andExpect(jsonPath("$.errors[0].field").value(invalidField))
        .andExpect(jsonPath("$.errors[0].code").value(expectedCode));

    then(itemApiPort).shouldHaveNoInteractions();
  }

  static Stream<Arguments> invalidCreatePayloads() {
    return Stream.of(
        arguments(
            "name",
            """
            {
              "type": "SNACK",
              "price": 1.5
            }
            """,
            "NotBlank"),
        arguments(
            "name",
            """
            {
              "name": "   ",
              "type": "SNACK",
              "price": 1.5
            }
            """,
            "NotBlank"),
        arguments(
            "type",
            """
            {
              "name": "Water",
              "price": 1.5
            }
            """,
            "NotNull"),
        arguments(
            "price",
            """
            {
              "name": "Water",
              "type": "SNACK",
              "price": 0
            }
            """,
            "Positive"),
        arguments(
            "price",
            """
            {
              "name": "Water",
              "type": "SNACK",
              "price": -1.5
            }
            """,
            "Positive"));
  }

  @Test
  void should_report_every_violation_of_an_invalid_create_payload() throws Exception {
    // Given / When / Then
    mockMvc
        .perform(
            post("/api/v1/items")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "",
                      "price": -1
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("3 errors found in [itemToCreateDto]"))
        .andExpect(jsonPath("$.errors[*].field", containsInAnyOrder("name", "type", "price")));

    then(itemApiPort).shouldHaveNoInteractions();
  }

  @Test
  void should_accept_create_payload_with_a_null_price() throws Exception {
    // Given
    var createdItem = aSnack("Chips", 2.1);

    given(itemApiPort.create(any(ItemToCreate.class))).willReturn(createdItem);

    // When / Then
    mockMvc
        .perform(
            post("/api/v1/items")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "Chips",
                      "type": "SNACK"
                    }
                    """))
        .andExpect(status().isCreated());

    var itemToCreate = ArgumentCaptor.forClass(ItemToCreate.class);
    then(itemApiPort).should().create(itemToCreate.capture());
    assertThat(itemToCreate.getValue().price()).isNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "-2.4"})
  void should_reject_update_payload_with_a_non_positive_price(String price) throws Exception {
    // Given / When / Then
    mockMvc
        .perform(
            put("/api/v1/items/{id}", UUID.randomUUID())
                .contentType(APPLICATION_JSON)
                .content("{ \"price\" : %s }".formatted(price)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("1 errors found in [itemToUpdateDto]"))
        .andExpect(jsonPath("$.errors[0].field").value("price"))
        .andExpect(jsonPath("$.errors[0].code").value("Positive"));

    then(itemApiPort).shouldHaveNoInteractions();
  }

  @Test
  void should_translate_resource_not_found_to_not_found_response() throws Exception {
    // Given
    var id = UUID.randomUUID();
    given(itemApiPort.read(new ItemId(id))).willThrow(new ResourceNotFound(new ItemId(id)));

    // When / Then
    mockMvc
        .perform(get("/api/v1/items/{id}", id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Resource not found with ID: " + new ItemId(id)));
  }

  private static UUID idOf(Item item) {
    return item.id().value();
  }
}
