package me.dahiorus.project.vending.core.service.validation.impl;

import static me.dahiorus.project.vending.core.service.validation.FieldValidationError.emptyOrNullValue;

import org.apache.commons.lang3.StringUtils;

import me.dahiorus.project.vending.common.HasLogger;
import me.dahiorus.project.vending.core.dao.AbstractDAO;
import me.dahiorus.project.vending.core.model.AbstractEntity;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;
import me.dahiorus.project.vending.core.service.validation.DtoValidator;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;

public abstract class DtoValidatorImpl<E extends AbstractEntity, D extends AbstractDTO<E>, R extends AbstractDAO<E>>
    implements DtoValidator<E, D>, HasLogger
{
  protected final R dao;

  protected DtoValidatorImpl(final R dao)
  {
    this.dao = dao;
  }

  @Override
  public ValidationResults validate(final D dto)
  {
    getLogger().trace("Validating {} data", dto);

    ValidationResults results = new ValidationResults();
    doValidate(dto, results);

    getLogger().debug("Validation errors for {}: {}", dto, results);

    return results;
  }

  protected void rejectIfEmpty(final String field, final Object value, final ValidationResults validationResults)
  {
    if (value == null)
    {
      validationResults.addError(emptyOrNullValue(field));
    }
  }

  protected void rejectIfBlank(final String field, final String value, final ValidationResults validationResults)
  {
    if (StringUtils.isBlank(value))
    {
      validationResults.addError(emptyOrNullValue(field));
    }
  }

  protected abstract void doValidate(D dto, ValidationResults results);
}
