package me.dahiorus.project.vending.core.service.impl;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import me.dahiorus.project.vending.common.HasLogger;
import me.dahiorus.project.vending.core.dao.AbstractDAO;
import me.dahiorus.project.vending.core.model.AbstractEntity;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;

public abstract class DtoValidator<E extends AbstractEntity, D extends AbstractDTO<E>> implements Validator, HasLogger
{
  protected static final String CODE_FIELD_EMPTY = "validation.constraints.field_empty";

  protected final AbstractDAO<E> dao;

  protected DtoValidator(final AbstractDAO<E> dao)
  {
    this.dao = dao;
  }

  @Override
  public boolean supports(final Class<?> clazz)
  {
    return getSupportedClass().equals(clazz);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void validate(final Object target, final Errors errors)
  {
    getLogger().traceEntry("Validating {} data", target);

    D dto = (D) target;
    doValidate(dto, errors);

    getLogger().traceExit("Validation results for {}: {}", errors.getAllErrors());
  }

  protected abstract Class<D> getSupportedClass();

  protected abstract void doValidate(D dto, Errors errors);
}
