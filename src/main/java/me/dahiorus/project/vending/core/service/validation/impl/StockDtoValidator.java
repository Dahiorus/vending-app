package me.dahiorus.project.vending.core.service.validation.impl;

import static me.dahiorus.project.vending.core.service.validation.FieldValidationError.fieldError;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import me.dahiorus.project.vending.core.dao.AbstractDAO;
import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.Stock;
import me.dahiorus.project.vending.core.model.dto.StockDTO;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;

@Component
public class StockDtoValidator extends DtoValidatorImpl<Stock, StockDTO>
{
  private static final Logger logger = LogManager.getLogger(StockDtoValidator.class);

  private static final String CODE_STOCK_ITEM_NOT_EXISTS = "validation.constraints.stock.item_not_found";

  private final AbstractDAO<Item> itemDao;

  @Autowired
  public StockDtoValidator(final AbstractDAO<Stock> dao, final AbstractDAO<Item> itemDao)
  {
    super(dao);
    this.itemDao = itemDao;
  }

  @Override
  public Logger getLogger()
  {
    return logger;
  }

  @Override
  protected void doValidate(final StockDTO dto, final ValidationResults results)
  {
    Item exampleProbe = new Item();
    exampleProbe.setId(dto.getItemId());
    exampleProbe.setName(dto.getItemName());

    // validate the item exists
    if (!itemDao.exists(Example.of(exampleProbe)))
    {
      results.addError(fieldError("itemId", CODE_STOCK_ITEM_NOT_EXISTS,
          "The item with ID " + dto.getItemId() + "  does not exist", dto.getItemId()));
      results.addError(fieldError("itemName", CODE_STOCK_ITEM_NOT_EXISTS,
          "The item named '" + dto.getItemName() + "' does not exist", dto.getItemName()));
    }

    Long quantity = dto.getQuantity();
    if (quantity == null || quantity < 1L)
    {
      results.addError(fieldError("quantity", "validation.constraints.stock.quantity_positive",
          "The quantity to provision must be positive", quantity));
    }
  }
}
