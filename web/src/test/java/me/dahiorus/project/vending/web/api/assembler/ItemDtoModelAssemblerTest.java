package me.dahiorus.project.vending.web.api.assembler;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

import me.dahiorus.project.vending.domain.model.ItemBuilder;
import me.dahiorus.project.vending.domain.model.ItemType;
import me.dahiorus.project.vending.domain.model.dto.ItemDto;

class ItemDtoModelAssemblerTest
{
  ItemDtoModelAssembler modelAssembler;

  @BeforeEach
  void setUp() throws Exception
  {
    modelAssembler = new ItemDtoModelAssembler();
  }

  @Test
  void dtoToEntityModel()
  {
    ItemDto dto = new ItemDto();
    dto.setId(UUID.randomUUID());
    dto.setType(ItemType.COLD_BAVERAGE);

    EntityModel<ItemDto> model = modelAssembler.toModel(dto);

    Assertions.assertEntityModel(model, dto);
  }

  @Test
  void itemWithPictureToEntityModel()
  {
    ItemDto dto = new ItemDto();
    dto.setId(UUID.randomUUID());
    dto.setType(null);

    ItemBuilder.builder()
        .id(UUID.randomUUID())
        .type(ItemType.COLD_BAVERAGE)
        .buildDto();
    dto.setPictureId(UUID.randomUUID());

    EntityModel<ItemDto> model = modelAssembler.toModel(dto);

    Assertions.assertEntityModel(model, dto, "picture");
  }
}
