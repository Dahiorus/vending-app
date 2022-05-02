package me.dahiorus.project.vending.core.service.validation.impl;

import static com.dahiorus.project.vending.util.TestUtils.anySpec;
import static com.dahiorus.project.vending.util.TestUtils.assertHasExactlyFieldErrors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dahiorus.project.vending.util.ItemBuilder;

import me.dahiorus.project.vending.core.dao.impl.ItemDaoImpl;
import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.ItemType;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;

@ExtendWith(MockitoExtension.class)
class ItemDtoValidatorTest
{
  @Mock
  ItemDaoImpl dao;

  ItemDtoValidator validator;

  ItemDTO dto;

  @BeforeEach
  void setUp() throws Exception
  {
    validator = new ItemDtoValidator(dao);
  }

  @Test
  void dtoIsValid()
  {
    dto = buildDto("Item", ItemType.FOOD, 1.3);

    ValidationResults results = validator.validate(dto);

    assertThat(results.hasError()).as("No error on %s", dto)
      .isFalse();
  }

  @ParameterizedTest(name = "Blank name [{0}] is not valid")
  @NullAndEmptySource
  @ValueSource(strings = "  ")
  void nameIsMandatory(final String name)
  {
    dto = buildDto(name, ItemType.FOOD, 1.3);

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "name", "validation.constraints.empty_value");
  }

  @Test
  void nameIsUnique()
  {
    Item duplicate = new Item();
    duplicate.setName("Item");
    duplicate.setId(UUID.randomUUID());
    when(dao.findOne(anySpec())).thenReturn(Optional.of(duplicate));

    dto = buildDto(duplicate.getName(), ItemType.FOOD, 1.3);

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "name", "validation.constraints.not_unique");
  }

  @Test
  void typeIsMandatory()
  {
    dto = buildDto("Drink", null, 1.3);

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "type", "validation.constraints.empty_value");
  }

  @ParameterizedTest(name = "Price [{0}] is not valid")
  @NullSource
  @ValueSource(doubles = -1.5)
  void priceMustBePositiveAndIsMandatory(final Double price)
  {
    dto = buildDto("Drink", ItemType.COLD_BAVERAGE, null);

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "price", "validation.constraints.item.price_positive");
  }

  ItemDTO buildDto(final String name, final ItemType type, final Double price)
  {
    return ItemBuilder.builder()
      .name(name)
      .type(type)
      .price(price)
      .buildDto();
  }
}
