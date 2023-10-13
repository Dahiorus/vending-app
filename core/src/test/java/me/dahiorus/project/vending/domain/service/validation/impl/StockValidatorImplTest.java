package me.dahiorus.project.vending.domain.service.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import me.dahiorus.project.vending.domain.model.ItemBuilder;
import me.dahiorus.project.vending.domain.model.ItemType;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.VendingMachineBuilder;
import me.dahiorus.project.vending.domain.model.dto.ItemDto;
import me.dahiorus.project.vending.domain.model.dto.StockQuantityDto;
import me.dahiorus.project.vending.domain.service.validation.ValidationError;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;

class StockValidatorImplTest
{
  StockValidatorImpl validator;

  @BeforeEach
  void setUp()
  {
    validator = new StockValidatorImpl();
  }

  @Test
  void validStock()
  {
    ValidationResults results = validator.validate(new StockQuantityDto(
      buildItem(ItemType.COLD_BAVERAGE), 10),
      buildMachine(ItemType.COLD_BAVERAGE));

    assertThat(results.count()).isZero();
  }

  @Test
  void typeNotMatch()
  {
    ValidationResults results = validator.validate(new StockQuantityDto(buildItem(ItemType.FOOD), 10),
      buildMachine(ItemType.COLD_BAVERAGE));

    assertThat(results.getObjectErrors()).extracting(ValidationError::getCode)
      .containsExactly("validation.constraints.stock.invalid_item");
  }

  @Test
  void quantityMustBePositive()
  {
    ValidationResults results = validator.validate(new StockQuantityDto(
      buildItem(ItemType.COLD_BAVERAGE), -1),
      buildMachine(ItemType.COLD_BAVERAGE));

    assertThat(results.getFieldErrors("quantity"))
      .extracting(ValidationError::getCode)
      .containsExactly("validation.constraints.stock.quantity_positive");
  }

  static VendingMachine buildMachine(final ItemType type)
  {
    return VendingMachineBuilder.builder()
      .itemType(type)
      .build();
  }

  static ItemDto buildItem(final ItemType type)
  {
    return ItemBuilder.builder()
      .type(type)
      .buildDto();
  }
}
