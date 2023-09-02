package me.dahiorus.project.vending.web.api.assembler;

import static me.dahiorus.project.vending.web.api.assembler.Assertions.assertRelationLinks;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

import me.dahiorus.project.vending.domain.model.dto.CommentDto;

class CommentDtoModelAssemblerTest
{
  CommentDtoModelAssembler modelAssembler;

  @BeforeEach
  void setUp() throws Exception
  {
    modelAssembler = new CommentDtoModelAssembler();
  }

  @Test
  void dtoToEntityModel()
  {
    CommentDto dto = new CommentDto();
    dto.setId(UUID.randomUUID());
    dto.setVendingMachineId(UUID.randomUUID());

    EntityModel<CommentDto> model = modelAssembler.toModel(dto);

    assertAll(() -> assertThat(model.getContent()).as("Model contains DTO")
      .isEqualTo(dto),
      () -> assertRelationLinks(model, "machine"));
  }
}
