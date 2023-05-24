package me.dahiorus.project.vending.web.api.assembler;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

import me.dahiorus.project.vending.domain.model.ItemType;
import me.dahiorus.project.vending.domain.model.dto.ItemDTO;
import me.dahiorus.project.vending.util.ItemBuilder;

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
    ItemDTO dto = ItemBuilder.builder()
      .id(UUID.randomUUID())
      .type(ItemType.COLD_BAVERAGE)
      .buildDto();

    EntityModel<ItemDTO> model = modelAssembler.toModel(dto);

    Assertions.assertEntityModel(model, dto);
  }

  @Test
  void itemWithPictureToEntityModel()
  {
    ItemDTO dto = ItemBuilder.builder()
      .id(UUID.randomUUID())
      .type(ItemType.COLD_BAVERAGE)
      .buildDto();
    dto.setPictureId(UUID.randomUUID());

    EntityModel<ItemDTO> model = modelAssembler.toModel(dto);

    Assertions.assertEntityModel(model, dto, "picture");
  }
}
