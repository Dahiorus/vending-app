package me.dahiorus.project.vending.domain.service.validation.impl;

import static me.dahiorus.project.vending.domain.service.validation.FieldValidationError.fieldError;
import static me.dahiorus.project.vending.domain.service.validation.ValidationError.getFullCode;
import static me.dahiorus.project.vending.domain.service.validation.ValidationError.objectError;

import org.springframework.stereotype.Component;

import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.service.validation.StockValidator;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;

@Component
public class StockValidatorImpl implements StockValidator
{
  @Override
  public ValidationResults validate(final Item item, final Integer quantity, final VendingMachine machine)
  {
    ValidationResults validationResults = new ValidationResults();

    // the item type must be the same as the machine type
    if (machine.getType() != item.getType())
    {
      validationResults.addError(objectError(getFullCode("stock.invalid_item"),
        "Unable to add a stock of " + item.getName() + " in the machine " + machine.getId(), item.getName(),
        machine.getId()));
    }

    if (quantity == null || quantity < 1L)
    {
      validationResults.addError(fieldError("quantity", getFullCode("stock.quantity_positive"),
        "The quantity to provision must be positive", quantity));
    }

    return validationResults;
  }
}
