package me.dahiorus.project.vending.infrastructure.rest.assembler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;
import me.dahiorus.project.vending.domain.item.entity.ItemType;
import me.dahiorus.project.vending.infrastructure.rest.entity.item.ItemDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;

@ExtendWith(MockitoExtension.class)
class ItemPagedModelAssemblerTest {
  @Mock
  RepresentationModelAssembler<ItemDto, EntityModel<ItemDto>> itemAssembler;

  ItemPagedModelAssembler assembler;

  @BeforeEach
  void setUp() {
    var pageAssembler =
        new PagedResourcesAssembler<ItemDto>(
            null,
            UriComponentsBuilder.fromUriString("http://localhost/api/v1/items").build());
    assembler = new ItemPagedModelAssembler(pageAssembler, itemAssembler);
  }

  @Test
  void should_apply_item_assembler_to_each_element_of_the_page() {
    // Given
    var item =
        new ItemDto(UUID.randomUUID(), "Water", ItemType.COLD_BEVERAGE, BigDecimal.valueOf(1.5));
    var page = new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);

    given(itemAssembler.toModel(item)).willReturn(EntityModel.of(item));

    // When
    var pagedModel = assembler.toModel(page);

    // Then
    then(itemAssembler).should().toModel(item);
    assertThat(pagedModel.getContent()).hasSize(1);
  }

  @Test
  void should_add_create_affordance_on_the_collection_self_link() {
    // Given
    var page = new PageImpl<>(List.<ItemDto>of(), PageRequest.of(0, 10), 0);

    // When
    var pagedModel = assembler.toModel(page);

    // Then
    var selfLink = pagedModel.getRequiredLink("self");
    var affordanceMethods =
        selfLink.getAffordances().stream()
            .flatMap(affordance -> StreamSupport.stream(affordance.spliterator(), false))
            .map(model -> model.getName() + ":" + model.getHttpMethod())
            .toList();
    assertThat(affordanceMethods).contains("create:" + HttpMethod.POST);
    then(itemAssembler).shouldHaveNoInteractions();
  }
}
