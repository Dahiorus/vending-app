package me.dahiorus.project.vending.infrastructure.rest.assembler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;
import me.dahiorus.project.vending.infrastructure.rest.entity.machine.VendingMachineDto;
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
class VendingMachinePagedModelAssemblerTest {
  @Mock
  RepresentationModelAssembler<VendingMachineDto, EntityModel<VendingMachineDto>> itemAssembler;

  VendingMachinePagedModelAssembler assembler;

  @BeforeEach
  void setUp() {
    var pageAssembler =
        new PagedResourcesAssembler<VendingMachineDto>(
            null,
            UriComponentsBuilder.fromUriString("http://localhost/api/v1/vending-machines")
                .build());
    assembler = new VendingMachinePagedModelAssembler(pageAssembler, itemAssembler);
  }

  @Test
  void should_apply_item_assembler_to_each_element_of_the_page() {
    // Given
    var vendingMachine =
        new VendingMachineDto(
            UUID.randomUUID(), "SN-001", null, null, null, null, null, null, null, null, null);
    var page = new PageImpl<>(List.of(vendingMachine), PageRequest.of(0, 10), 1);

    given(itemAssembler.toModel(vendingMachine)).willReturn(EntityModel.of(vendingMachine));

    // When
    var pagedModel = assembler.toModel(page);

    // Then
    then(itemAssembler).should().toModel(vendingMachine);
    assertThat(pagedModel.getContent()).hasSize(1);
  }

  @Test
  void should_add_create_affordance_on_the_collection_self_link() {
    // Given
    var page = new PageImpl<>(List.<VendingMachineDto>of(), PageRequest.of(0, 10), 0);

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
