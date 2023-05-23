package me.dahiorus.project.vending.domain.service.validation.impl;

import static me.dahiorus.project.vending.domain.service.validation.FieldValidationError.fieldError;
import static me.dahiorus.project.vending.domain.service.validation.FieldValidationError.notUniqueValue;
import static me.dahiorus.project.vending.domain.service.validation.ValidationError.getFullCode;

import java.math.BigDecimal;

import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.dao.DAO;
import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.Item_;
import me.dahiorus.project.vending.domain.model.dto.ItemDTO;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;

@Log4j2
@Component
public class ItemDtoValidator extends DtoValidatorImpl<Item, ItemDTO, DAO<Item>>
{
  public ItemDtoValidator(final DAO<Item> dao)
  {
    super(dao);
  }

  @Override
  public Logger getLogger()
  {
    return log;
  }

  @Override
  protected void doValidate(final ItemDTO dto, final ValidationResults results)
  {
    String name = dto.getName();

    // validate mandatory fields
    rejectIfBlank(Item_.NAME, name, results);
    rejectIfInvalidLength(Item_.NAME, name, 255, results);
    rejectIfEmpty(Item_.TYPE, dto.getType(), results);

    // validate price is positive
    BigDecimal price = dto.getPrice();
    if (price == null || price.compareTo(BigDecimal.ZERO) <= 0)
    {
      results.addError(fieldError(Item_.PRICE, getFullCode("item.price_positive"),
        "The price must be a positive number", price));
    }

    // validate name is unique
    if (!results.hasFieldError(Item_.NAME) &&
      otherExists(dto.getId(), (root, query, cb) -> cb.equal(root.get(Item_.name), name)))
    {
      results.addError(notUniqueValue(Item_.NAME, name));
    }

  }
}
