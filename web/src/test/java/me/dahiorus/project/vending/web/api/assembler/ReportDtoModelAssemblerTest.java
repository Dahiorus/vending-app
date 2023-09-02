package me.dahiorus.project.vending.web.api.assembler;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

import me.dahiorus.project.vending.domain.model.dto.ReportDto;
import me.dahiorus.project.vending.domain.model.dto.ReportStockDto;

class ReportDtoModelAssemblerTest
{
  ReportDtoModelAssembler modelAssembler;

  @BeforeEach
  void setUp() throws Exception
  {
    modelAssembler = new ReportDtoModelAssembler();
  }

  @Test
  void dtoToEntityModel()
  {
    ReportDto dto = new ReportDto();
    dto.setId(UUID.randomUUID());
    dto.setReportStocks(List.of(new ReportStockDto()));

    EntityModel<ReportDto> model = modelAssembler.toModel(dto);

    Assertions.assertEntityModel(model, dto);
  }
}
