package me.dahiorus.project.vending.domain.service.validation.impl;

import static me.dahiorus.project.vending.domain.service.validation.FieldValidationError.emptyOrNullValue;
import static me.dahiorus.project.vending.domain.service.validation.FieldValidationError.maxLength;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.dahiorus.project.vending.common.HasLogger;
import me.dahiorus.project.vending.domain.dao.Dao;
import me.dahiorus.project.vending.domain.model.AbstractEntity;
import me.dahiorus.project.vending.domain.model.AbstractEntity_;
import me.dahiorus.project.vending.domain.model.dto.AbstractDto;
import me.dahiorus.project.vending.domain.service.validation.DtoValidator;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class DtoValidatorImpl<E extends AbstractEntity, D extends AbstractDto<E>, R extends Dao<E>>
  implements DtoValidator<D>, HasLogger
{
  protected final R dao;

  @Override
  public ValidationResults validate(final D dto)
  {
    getLogger().trace("Validating {} data", dto);

    ValidationResults results = new ValidationResults();
    doValidate(dto, results);

    getLogger().debug("Validation errors for {}: {}", dto, results);

    return results;
  }

  protected void rejectIfEmpty(final String field, final Object value,
    final ValidationResults validationResults)
  {
    if (value == null)
    {
      validationResults.addError(emptyOrNullValue(field));
    }
  }

  protected void rejectIfBlank(final String field, final String value,
    final ValidationResults validationResults)
  {
    if (StringUtils.isBlank(value))
    {
      validationResults.addError(emptyOrNullValue(field));
    }
  }

  protected void rejectIfInvalidLength(final String field, final String value, final int maxLength,
    final ValidationResults validationResults)
  {
    if (StringUtils.length(value) > maxLength)
    {
      validationResults.addError(maxLength(field, maxLength));
    }
  }

  protected boolean otherExists(final UUID id, final Specification<E> specification)
  {
    return dao.count(Specification.where(specification)
      .and(idNotEqualOrNotNull(id))) != 0;
  }

  private Specification<E> idNotEqualOrNotNull(final UUID id)
  {
    if (id == null)
    {
      return (root, query, cb) -> cb.isNotNull(root.get(AbstractEntity_.id));
    }

    return (root, query, cb) -> cb.notEqual(root.get(AbstractEntity_.id), id);
  }

  protected abstract void doValidate(D dto, ValidationResults results);
}
