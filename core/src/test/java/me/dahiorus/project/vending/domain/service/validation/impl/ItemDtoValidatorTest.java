package me.dahiorus.project.vending.domain.service.validation.impl;

import static me.dahiorus.project.vending.util.TestUtils.anySpec;
import static me.dahiorus.project.vending.util.ValidationTestUtils.assertHasExactlyFieldErrors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.dahiorus.project.vending.domain.dao.ItemDao;
import me.dahiorus.project.vending.domain.model.ItemType;
import me.dahiorus.project.vending.domain.model.dto.ItemDto;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;
import me.dahiorus.project.vending.util.ItemBuilder;

@ExtendWith(MockitoExtension.class)
class ItemDtoValidatorTest
{
  @Mock
  ItemDao dao;

  ItemDtoValidator validator;

  ItemDto dto;

  @BeforeEach
  void setUp() throws Exception
  {
    validator = new ItemDtoValidator(dao);
  }

  @Test
  void dtoIsValid()
  {
    dto = buildDto("Item", ItemType.FOOD, BigDecimal.valueOf(1.3));

    ValidationResults results = validator.validate(dto);

    assertThat(results.hasError()).as("No error on %s", dto)
      .isFalse();
  }

  @ParameterizedTest(name = "Blank name [{0}] is not valid")
  @NullAndEmptySource
  @ValueSource(strings = "  ")
  void nameIsMandatory(final String name)
  {
    dto = buildDto(name, ItemType.FOOD, BigDecimal.valueOf(1.3));

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "name", "validation.constraints.empty_value");
  }

  @Test
  void nameIsUnique()
  {
    when(dao.count(anySpec())).thenReturn(1L);
    dto = buildDto("Item", ItemType.FOOD, BigDecimal.valueOf(1.3));

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "name", "validation.constraints.not_unique");
  }

  @Test
  void nameHasMaxLength()
  {
    dto = buildDto(RandomStringUtils.randomAlphanumeric(256), ItemType.FOOD, BigDecimal.valueOf(1.5));

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "name", "validation.constraints.max_length");
  }

  @Test
  void typeIsMandatory()
  {
    dto = buildDto("Drink", null, BigDecimal.valueOf(1.3));

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "type", "validation.constraints.empty_value");
  }

  @ParameterizedTest(name = "Price [{0}] is not valid")
  @NullSource
  @ValueSource(doubles = -1.5)
  void priceMustBePositiveAndIsMandatory(final Double price)
  {
    dto = buildDto("Drink", ItemType.COLD_BAVERAGE, price == null ? null : BigDecimal.valueOf(price));

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "price", "validation.constraints.item.price_positive");
  }

  ItemDto buildDto(final String name, final ItemType type, final BigDecimal price)
  {
    return ItemBuilder.builder()
      .name(name)
      .type(type)
      .price(price)
      .buildDto();
  }
}
