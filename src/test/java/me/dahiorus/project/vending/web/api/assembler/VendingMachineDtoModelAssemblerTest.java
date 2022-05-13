package me.dahiorus.project.vending.web.api.assembler;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

import me.dahiorus.project.vending.core.model.ItemType;
import me.dahiorus.project.vending.core.model.dto.VendingMachineDTO;
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
    VendingMachineDTO dto = VendingMachineBuilder.builder()
      .id(UUID.randomUUID())
      .address("1 rune Bidon")
      .itemType(ItemType.COLD_BAVERAGE)
      .serialNumber("1230456")
      .buildDto();

    EntityModel<VendingMachineDTO> model = modelAssembler.toModel(dto);

    Assertions.assertEntityModel(model, dto, "stocks", "comments", "report", "reset");
  }
}
