package me.dahiorus.project.vending.web.api.assembler;

import static me.dahiorus.project.vending.web.api.assembler.Assertions.assertRelationLinks;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

import me.dahiorus.project.vending.domain.model.dto.StockDTO;

class StockDtoModelAssemblerTest
{
  StockDtoModelAssembler modelAssembler;

  @BeforeEach
  void setUp() throws Exception
  {
    modelAssembler = new StockDtoModelAssembler();
  }

  @Test
  void dtoToEntityModel()
  {
    StockDTO dto = new StockDTO();
    dto.setId(UUID.randomUUID());
    dto.setVendingMachineId(UUID.randomUUID());
    dto.setItemId(UUID.randomUUID());

    EntityModel<StockDTO> model = modelAssembler.toModel(dto);

    assertAll(() -> assertThat(model.getContent()).as("Model contains DTO")
      .isEqualTo(dto),
        () -> assertRelationLinks(model, "machine", "item", "provision", "purchase"));
  }
}
