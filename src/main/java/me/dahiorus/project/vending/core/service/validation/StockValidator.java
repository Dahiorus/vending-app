package me.dahiorus.project.vending.core.service.validation;

import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.VendingMachine;

public interface StockValidator
{
  ValidationResults validate(final Item item, final Integer quantity,
      final VendingMachine machine);
}
