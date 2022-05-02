package me.dahiorus.project.vending.core.service.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dahiorus.project.vending.util.ItemBuilder;
import com.dahiorus.project.vending.util.VendingMachineBuilder;

import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.ItemType;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.service.validation.ValidationError;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;

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
    ValidationResults results = validator.validate(buildItem(ItemType.COLD_BAVERAGE), 10,
        buildMachine(ItemType.COLD_BAVERAGE));

    assertThat(results.count()).isZero();
  }

  @Test
  void typeNotMatch()
  {
    ValidationResults results = validator.validate(buildItem(ItemType.FOOD), 10,
        buildMachine(ItemType.COLD_BAVERAGE));

    assertThat(results.getObjectErrors()).extracting(ValidationError::getCode)
      .containsExactly("validation.constraints.stock.invalid_item");
  }

  @Test
  void quantityMustBePositive()
  {
    ValidationResults results = validator.validate(buildItem(ItemType.COLD_BAVERAGE), -1,
        buildMachine(ItemType.COLD_BAVERAGE));

    assertThat(results.getFieldErrors("quantity")).extracting(ValidationError::getCode)
      .containsExactly("validation.constraints.stock.quantity_positive");
  }

  static VendingMachine buildMachine(final ItemType type)
  {
    return VendingMachineBuilder.builder()
      .itemType(type)
      .build();
  }

  static Item buildItem(final ItemType type)
  {
    return ItemBuilder.builder()
      .type(type)
      .build();
  }
}
