package me.dahiorus.project.vending.core.service.validation.impl;

import static me.dahiorus.project.vending.core.service.validation.FieldValidationError.fieldError;
import static me.dahiorus.project.vending.core.service.validation.FieldValidationError.notUniqueValue;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import me.dahiorus.project.vending.core.dao.ItemDAO;
import me.dahiorus.project.vending.core.model.Item_;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;

@Component
public class ItemDtoValidator extends DtoValidatorImpl<ItemDTO, ItemDAO>
{
  private static final Logger logger = LogManager.getLogger(ItemDtoValidator.class);

  public ItemDtoValidator(final ItemDAO dao)
  {
    super(dao);
  }

  @Override
  public Logger getLogger()
  {
    return logger;
  }

  @Override
  protected void doValidate(final ItemDTO dto, final ValidationResults results)
  {
    String name = dto.getName();

    // validate mandatory fields
    rejectIfBlank(Item_.NAME, name, results);
    rejectIfEmpty(Item_.TYPE, dto.getType(), results);

    // validate price is positive
    Double price = dto.getPrice();
    if (price == null || price < .0)
    {
      results.addError(fieldError(Item_.PRICE, "validation.constraints.item.price_positive",
          "The price must be a positive number", price));
    }

    // validate name is unique
    if (!results.hasFieldError(Item_.NAME))
    {
      dao.findOne((root, query, cb) -> cb.equal(root.get("name"), name))
        .ifPresent(other -> {
          if (!Objects.equals(dto.getId(), other.getId()))
          {
            results.addError(notUniqueValue(Item_.NAME, name));
          }
        });
    }

  }
}
