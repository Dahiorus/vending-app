package me.dahiorus.project.vending.core.service.validation.impl;

import static com.dahiorus.project.vending.util.TestUtils.assertHasExactlyFieldErrors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;

import me.dahiorus.project.vending.core.dao.ModelDaos.ItemDAO;
import me.dahiorus.project.vending.core.dao.ModelDaos.StockDAO;
import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.dto.StockDTO;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;

@ExtendWith(MockitoExtension.class)
class StockDtoValidatorTest
{
  @Mock
  StockDAO dao;

  @Mock
  ItemDAO itemDao;

  StockDtoValidator validator;

  StockDTO dto;

  @Captor
  ArgumentCaptor<Example<Item>> itemExampleArg;

  @BeforeEach
  void setUp() throws Exception
  {
    validator = new StockDtoValidator(dao, itemDao);
  }

  StockDTO buildDto(final UUID itemId, final Long quantity)
  {
    StockDTO dto = new StockDTO();
    dto.setItemId(itemId);
    dto.setQuantity(quantity);

    return dto;
  }

  @Test
  void dtoIsValid()
  {
    dto = buildDto(UUID.randomUUID(), 15L);
    when(itemDao.exists(itemExampleArg.capture())).thenReturn(true);

    ValidationResults results = validator.validate(dto);

    assertAll(() -> assertThat(results.hasError()).as("No error on %s", dto)
      .isFalse(),
        () -> assertThat(itemExampleArg.getValue()).extracting(Example::getProbe)
          .hasFieldOrPropertyWithValue("id", dto.getItemId()));
  }

  @ParameterizedTest(name = "Quantity [{0}] is not valid")
  @NullSource
  @ValueSource(longs = -5L)
  void quantityMustBePositive(final Long quantity)
  {
    dto = buildDto(UUID.randomUUID(), quantity);
    when(itemDao.exists(itemExampleArg.capture())).thenReturn(true);

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "quantity", "validation.constraints.stock.quantity_positive");
  }

  @Test
  void itemMustExist()
  {
    dto = buildDto(UUID.randomUUID(), 5L);
    when(itemDao.exists(itemExampleArg.capture())).thenReturn(false);

    ValidationResults results = validator.validate(dto);

    assertAll(() -> assertHasExactlyFieldErrors(results, "itemId", "validation.constraints.stock.item_not_found"),
        () -> assertHasExactlyFieldErrors(results, "itemName", "validation.constraints.stock.item_not_found"));
  }
}
