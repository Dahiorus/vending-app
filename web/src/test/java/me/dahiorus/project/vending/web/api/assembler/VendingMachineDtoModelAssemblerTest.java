package me.dahiorus.project.vending.web.api.assembler;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

import me.dahiorus.project.vending.domain.model.ItemType;
import me.dahiorus.project.vending.domain.model.dto.VendingMachineDto;
import me.dahiorus.project.vending.util.VendingMachineBuilder;

class VendingMachineDtoModelAssemblerTest
{
  VendingMachineDtoModelAssembler modelAssembler;

  @BeforeEach
  void setUp() throws Exception
  {
    modelAssembler = new VendingMachineDtoModelAssembler();
  }

  @Test
  void dtoToEntityModel()
  {
    VendingMachineDto dto = VendingMachineBuilder.builder()
      .id(UUID.randomUUID())
      .address("1 rune Bidon")
      .itemType(ItemType.COLD_BAVERAGE)
      .serialNumber("1230456")
      .buildDto();

    EntityModel<VendingMachineDto> model = modelAssembler.toModel(dto);

    Assertions.assertEntityModel(model, dto, "stocks", "comments", "report", "reset");
  }
}
