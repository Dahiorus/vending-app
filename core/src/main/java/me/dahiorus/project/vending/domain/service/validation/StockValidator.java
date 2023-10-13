package me.dahiorus.project.vending.domain.service.validation;

import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.dto.StockQuantityDto;

public interface StockValidator
{
  ValidationResults validate(final StockQuantityDto stockQuantity,
    final VendingMachine machine);
}
