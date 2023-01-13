package me.dahiorus.project.vending.domain.service.validation;

import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.VendingMachine;

public interface StockValidator
{
  ValidationResults validate(final Item item, final Integer quantity,
    final VendingMachine machine);
}
