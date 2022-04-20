package me.dahiorus.project.vending.core.service.impl;

import static org.springframework.validation.ValidationUtils.rejectIfEmpty;
import static org.springframework.validation.ValidationUtils.rejectIfEmptyOrWhitespace;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import me.dahiorus.project.vending.core.dao.AbstractDAO;
import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.Item_;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;

@Component
public class ItemDtoValidator extends DtoValidator<Item, ItemDTO>
{
  private static final Logger logger = LogManager.getLogger(ItemDtoValidator.class);

  public ItemDtoValidator(final AbstractDAO<Item> dao)
  {
    super(dao);
  }

  @Override
  protected Class<ItemDTO> getSupportedClass()
  {
    return ItemDTO.class;
  }

  @Override
  protected void doValidate(final ItemDTO dto, final Errors errors)
  {
    // validate mandatory fields
    rejectIfEmptyOrWhitespace(errors, Item_.NAME, CODE_FIELD_EMPTY, "The name is mandatory");
    rejectIfEmpty(errors, Item_.TYPE, CODE_FIELD_EMPTY, "The type is mandatory");

    // validate price is positive
    Double price = dto.getPrice();
    if (price == null || price < .0)
    {
      errors.rejectValue(Item_.PRICE, "validation.constraints.item.price_positive",
          "The price must be a positive number");
    }

    // validate name is unique
    final String name = dto.getName();

    if (StringUtils.isEmpty(name))
    {
      return;
    }

    dao.findOne((root, query, cb) -> cb.equal(root.get("name"), name))
      .ifPresent(other -> {
        if (!Objects.equals(dto.getId(), other.getId()))
        {
          errors.rejectValue("name", "validation.constraints.item.unique_name",
              "The name must be unique");
        }
      });
  }

  @Override
  public Logger getLogger()
  {
    return logger;
  }
}
